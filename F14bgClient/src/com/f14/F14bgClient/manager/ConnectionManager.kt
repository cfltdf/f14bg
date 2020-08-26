package com.f14.F14bgClient.manager

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bgClient.PlayerHandler
import com.f14.F14bgClient.User
import com.f14.bg.action.BgResponse
import com.f14.utils.ByteUtil
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.nio.aConnect
import kotlinx.coroutines.experimental.nio.aWrite
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

class ConnectionManager {
    protected var log = Logger.getLogger(this.javaClass)
    protected var handler: PlayerHandler? = null
    protected val pool = newSingleThreadContext("f14bgClient")
    var localUser: User? = null

    /**
     * 创建与服务器的连接
     * @param host
     * @param port
     * @throws Exception
     */
    @Throws(Exception::class)
    fun connect(host: String, port: Int) {
        // 如果已经存在连接,则切断当前连接
        if (handler != null && !handler!!.isClosed) {
            this.close()
        }
        val socket = this.createSocket(host, port)
        socket.keepAlive = true
        handler = PlayerHandler(socket)
        // 发送校验字节
        val bs = ByteUtil.itob2(CmdConst.APPLICATION_FLAG)
        write(bs)
        // 接收器创建完成后,开启线程监听服务器信息(使用UI线程)
        launch(pool){
            handler!!.run()
        }
    }

    /**
     * 按照配置文件创建socket对象
     * @param host
     * @param port
     * @return
     * @throws IOException
     * @throws UnknownHostException
     */
    @Throws(Exception::class)
    protected fun createSocket(host: String, port: Int): Socket {
        val socket: Socket
        val proxy_active = ManagerContainer.propertiesManager.getLocalProperty("proxy_active")
        if ("true" == proxy_active) {
            // 使用代理
            socket = this.createHttpProxySocket(host, port) ?: throw Exception("连接创建失败!")
        } else {
            // 不使用代理
            socket = Socket(host, port)
        }
        return socket
    }

    /**
     * 创建HTTP隧道的socket对象
     * @param host
     * @param port
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun createHttpProxySocket(host: String, port: Int): Socket? {
        val proxy_ip = ManagerContainer.propertiesManager.getLocalProperty("proxy_ip")
        val proxy_port = Integer.valueOf(ManagerContainer.propertiesManager.getLocalProperty("proxy_port"))!!.toInt()

        val socket = Socket(proxy_ip, proxy_port)
        var str = "CONNECT $host:$port HTTP/1.0\r\n"
        str += "HOST $host:$port\r\n"
        str += "\r\n"
        socket.getOutputStream().write(str.toByteArray())

        // socket.getOutputStream().write(str.getBytes());

        /*
		 * ChannelBuffer cb = new DynamicChannelBuffer(128); int read = 0;
		 * byte[] ba = new byte[128];
		 */

        var cmd: String
        val isr = InputStreamReader(socket.getInputStream())
        val br = BufferedReader(isr)
        do {
            cmd = br.readLine()
            if (cmd == null) {
                break;
            }
            log.debug(cmd)
            if (cmd.startsWith("HTTP") && cmd.indexOf(" 200 ") > -1) {
                return socket
            }
        } while (true)

        /*
		 * while((read = socket.getInputStream().read(ba))>-1){
		 * cb.writeBytes(ba, 0, read); String cmd = new
		 * String(cb.toByteBuffer().array()); log.debug(cmd); if(cmd.indexOf(
		 * " 200 ")>-1){ return socket; } }
		 */
        return null
    }

    /**
     * 向服务器发送字节
     * @param bytes
     */
    protected fun write(bytes: ByteArray) {
        try {
            this.handler!!.socket.getOutputStream().write(bytes)
        } catch (e: IOException) {
            log.error(e, e)
        }

    }

    /**
     * 向服务器发送指令
     * @param roomId
     * @param cmdstr
     * @throws IOException
     */
    @Throws(IOException::class)
    fun sendCommand(roomId: Int, cmdstr: String) {
        if (roomId < 0){
            return
        }
        val cmd = CmdFactory.createCommand(roomId, cmdstr)
        this.handler!!.sendCommand(cmd)
    }

    /**
     * 向服务器发送信息
     * @param res
     */
    fun sendResponse(roomId: Int, res: BgResponse) {
        val content = res.toPublicString()
        try {
            this.sendCommand(roomId, content)
        } catch (e: IOException) {
            log.error(e, e)
        }

    }

    /**
     * 向服务器发送信息
     * @param res
     */
    fun sendResponse(res: BgResponse) {
        val content = res.toPublicString()
        try {
            this.sendCommand(0, content)
        } catch (e: IOException) {
            log.error(e, e)
        }

    }

    /**
     * 关闭连接
     */
    fun close() {
        if (handler != null) {
            handler!!.close()
        }
    }

    /**
     * 判断是否设置了使用代理
     * @return
     */
    val isProxyActive: Boolean
        get() {
            val proxy_active = ManagerContainer.propertiesManager.getLocalProperty("proxy_active")
            return "true" == proxy_active
        }

    /**
     * 取得Http的Proxy对象,如果不使用代理,则返回null
     * @return
     */
    val httpProxy: Proxy?
        get() {
            if (this.isProxyActive) {
                val proxy_ip = ManagerContainer.propertiesManager.getLocalProperty("proxy_ip")
                val proxy_port = Integer.valueOf(ManagerContainer.propertiesManager.getLocalProperty("proxy_port"))!!
                        .toInt()
                val addr = InetSocketAddress(proxy_ip, proxy_port)
                val typeProxy = Proxy(Proxy.Type.HTTP, addr)
                return typeProxy
            } else {
                return null
            }
        }

}
