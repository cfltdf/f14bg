package com.f14.net.socket.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.f14.utils.ByteUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CommandSender {
	protected Logger log = Logger.getLogger(this.getClass());
	public Socket socket;
	protected List<ByteCommand> cmds = new ArrayList<ByteCommand>();
	protected SenderThread sendThread;

	public CommandSender(Socket socket) {
		this.socket = socket;
		sendThread = new SenderThread();
		sendThread.start();
	}

	/**
	 * 发送指令
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	public void sendCommand(ByteCommand cmd) {
		this.cmds.add(cmd);
		if (!this.sendThread.isSending) {
			synchronized (this.sendThread) {
				this.sendThread.notify();
			}
		}
	}

	/**
	 * 将cmd转换成byte数组
	 * 
	 * @param cmd
	 * @return
	 */
	public byte[] toByte(ByteCommand cmd) {
		ByteBuf bb = Unpooled.buffer();
		bb.writeBytes(ByteUtil.itob2(cmd.head));
		bb.writeBytes(ByteUtil.itob2(cmd.flag));
		bb.writeBytes(ByteUtil.itob4(cmd.roomId));
		bb.writeBytes(ByteUtil.itob2(cmd.zip));
		bb.writeBytes(ByteUtil.itob4(cmd.size));
		bb.writeBytes(cmd.contentBytes);
		bb.writeBytes(ByteUtil.itob2(cmd.tail));
		byte[] res = new byte[bb.readableBytes()];
		bb.readBytes(res);
		return res;
	}

	/**
	 * 判断连接是否已经关闭
	 * 
	 * @return
	 */
	public boolean isClosed() {
		return this.socket != null && this.socket.isClosed();
	}

	/**
	 * 关闭发送指令用的线程
	 */
	public void closeThread() {
		synchronized (this.sendThread) {
			this.sendThread.isOver = true;
			this.sendThread.notifyAll();
		}
	}

	/**
	 * 发送指令的线程
	 * 
	 * @author F14eagle
	 *
	 */
	protected class SenderThread extends Thread {
		boolean isSending = false;
		boolean isOver = false;

		int error = 0;
		/**
		 * 允许最大错误限制
		 */
		int errorLimit = 10;

		@Override
		public synchronized void run() {
			main: while (!isClosed() && !isOver) {
				try {
					if (cmds.isEmpty()) {
						this.wait();
						if (isClosed() || isOver) {
							break;
						}
					}
					isSending = true;
					// 发送在发送序列中的指令
					OutputStream os = socket.getOutputStream();
					int count = cmds.size();
					for (int i = 0; i < count; i++) {
						ByteCommand cmd = cmds.get(i);
						try {
							if (!cmd.isSent) {
								os.write(toByte(cmd));
								os.flush();
								log.debug("发送指令: " + cmd + " | 至 " + socket);
								cmd.isSent = true;
							}
						} catch (IOException e) {
							if (checkError()) {
								// 如果超出重试次数,则结束发送
								break main;
							} else {
								// 否则尝试重发
								continue main;
							}
						}
					}
					// 移除所有已发送的指令
					for (int i = count - 1; i >= 0; i--) {
						ByteCommand cmd = cmds.get(i);
						if (cmd.isSent) {
							cmds.remove(i);
						}
					}
					// 设置发送标记为否
					isSending = false;
					// 顺利走完流程时,错误次数清零
					error = 0;
				} catch (InterruptedException e) {
					log.error("发送指令线程等待时发生错误!", e);
					if (checkError()) {
						break;
					}
				} catch (IOException e) {
					log.warn("远程连接发生错误!");
					if (checkError()) {
						break;
					}
				}
			}
			log.debug(socket + " 发送指令线程运行结束!");
		}

		/**
		 * 检查错误次数是否超过限制
		 * 
		 * @return
		 */
		private boolean checkError() {
			error++;
			if (error >= errorLimit) {
				// 如果超出错误允许的次数限制,则切断socket连接
				try {
					log.error(socket + " 超过最大重试次数,切断连接!");
					socket.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
			return error >= errorLimit;
		}
	}

	public void setName(String name) {
		this.sendThread.setName(name + "_SenderThread");
	}
}
