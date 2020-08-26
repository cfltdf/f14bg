package com.f14.utils

import org.apache.log4j.Logger
import java.io.*

object FileUtils {
    var log = Logger.getLogger(FileUtils::class.java)

    /**
     * 新建目录

     * @param folderPath
     * *            String 如 c:/fqf
     * *
     * @return boolean
     */
    fun newFolder(folderPath: String) {
        try {
            var filePath = folderPath
            filePath = filePath.toString()
            val myFilePath = java.io.File(filePath)
            if (!myFilePath.exists()) {
                myFilePath.mkdir()
            }
        } catch (e: Exception) {
            log.error("新建文件夹出错!", e)
        }

    }

    /**
     * 新建文件

     * @param filePathAndName
     * *            String 文件路径及名称 如c:/fqf.txt
     * *
     * @param content
     * *
     * @return boolean
     */
    @JvmOverloads fun newFile(path: String, content: String? = null): File? {
        try {
            val file = File(path)
            val dir = file.parentFile
            if (!dir.exists()) {
                dir.mkdirs()
            }
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            if (!StringUtils.isEmpty(content)) {
                val resultFile = FileWriter(file)
                val myFile = PrintWriter(resultFile)
                myFile.println(content)
                resultFile.close()
            }
            return file
        } catch (e: Exception) {
            log.error("新建文件出错!", e)
            return null
        }

    }

    /**
     * 删除文件

     * @param filePathAndName
     * *            String 文件路径及名称 如c:/fqf.txt
     * *
     * @param fileContent
     * *            String
     * *
     * @return boolean
     */
    fun delFile(filePathAndName: String) {
        try {
            var filePath = filePathAndName
            filePath = filePath.toString()
            val myDelFile = java.io.File(filePath)
            myDelFile.delete()

        } catch (e: Exception) {
            log.error("删除文件出错!", e)
        }

    }

    /**
     * 删除文件夹 &nbsp;

     * @param filePathAndName
     * *            String 文件夹路径及名称 如c:/fqf
     * *
     * @param fileContent
     * *            String
     * *
     * @return boolean
     */
    fun delFolder(folderPath: String) {
        try {
            delAllFile(folderPath) // 删除完里面所有内容
            var filePath = folderPath
            filePath = filePath.toString()
            val myFilePath = java.io.File(filePath)
            myFilePath.delete() // 删除空文件夹

        } catch (e: Exception) {
            log.error("删除文件夹出错!", e)
        }

    }

    /**
     * 删除文件夹里面的所有文件

     * @param path
     * *            String 文件夹路径 如 c:/fqf
     */
    fun delAllFile(path: String) {
        val file = File(path)
        if (!file.exists()) {
            return
        }
        if (!file.isDirectory) {
            return
        }
        val tempList = file.list()
        var temp: File? = null
        for (i in tempList!!.indices) {
            if (path.endsWith(File.separator)) {
                temp = File(path + tempList[i])
            } else {
                temp = File(path + File.separator + tempList[i])
            }
            if (temp.isFile) {
                temp.delete()
            }
            if (temp.isDirectory) {
                delAllFile(path + "/" + tempList[i])// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i])// 再删除空文件夹
            }
        }
    }

    /**
     * 复制单个文件

     * @param oldPath
     * *            String 原文件路径 如：c:/fqf.txt
     * *
     * @param newPath
     * *            String 复制后路径 如：f:/fqf.txt
     * *
     * @return boolean
     */
    fun copyFile(oldPath: String, newPath: String) {
        try {
            var bytesum = 0
            var byteread = 0
            val oldfile = File(oldPath)
            if (oldfile.exists()) { // 文件存在时
                val newfile = File(newPath)
                val parentpath = newfile.parent
                val dir = File(parentpath)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val inStream = FileInputStream(oldPath) // 读入原文件
                val fs = FileOutputStream(newPath)
                val buffer = ByteArray(1444)
                // int length;
                do {
                    byteread = inStream.read(buffer)
                    if (byteread == -1) {
                        break
                    }
                    bytesum += byteread // 字节数 文件大小
                    fs.write(buffer, 0, byteread)
                } while (true)
                fs.close()
                inStream.close()
            }
        } catch (e: Exception) {
            log.error("复制文件出错!", e)
        }

    }

    /**
     * 复制整个文件夹内容

     * @param oldPath
     * *            String 原文件路径 如：c:/fqf
     * *
     * @param newPath
     * *            String 复制后路径 如：f:/fqf/ff
     * *
     * @return boolean
     */
    fun copyFolder(oldPath: String, newPath: String) {

        try {
            File(newPath).mkdirs() // 如果文件夹不存在 则建立新文件夹
            val a = File(oldPath)
            val file = a.list()
            var temp: File? = null
            for (i in file!!.indices) {
                if (oldPath.endsWith(File.separator)) {
                    temp = File(oldPath + file[i])
                } else {
                    temp = File(oldPath + File.separator + file[i])
                }

                if (temp.isFile) {
                    val input = FileInputStream(temp)
                    val output = FileOutputStream(newPath + "/" + temp.name.toString())
                    val b = ByteArray(1024 * 5)
                    var len: Int
                    do {
                        len = input.read(b)
                        if (len == -1) {
                            break
                        }
                        output.write(b, 0, len)
                    } while (true)
                    output.flush()
                    output.close()
                    input.close()
                }
                if (temp.isDirectory) {// 如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i])
                }
            }
        } catch (e: Exception) {
            log.error("复制文件夹出错!", e)
        }

    }

    /**
     * 移动文件到指定目录

     * @param oldPath
     * *            String 如：c:/fqf.txt
     * *
     * @param newPath
     * *            String 如：d:/fqf.txt
     */
    fun moveFile(oldPath: String, newPath: String) {
        copyFile(oldPath, newPath)
        delFile(oldPath)

    }

    /**
     * 移动文件到指定目录

     * @param oldPath
     * *            String 如：c:/fqf.txt
     * *
     * @param newPath
     * *            String 如：d:/fqf.txt
     */
    fun moveFolder(oldPath: String, newPath: String) {
        copyFolder(oldPath, newPath)
        delFolder(oldPath)

    }
}
/**
 * 新建文件

 * @param filePathAndName
 * *            String 文件路径及名称 如c:/fqf.txt
 * *
 * @return boolean
 */