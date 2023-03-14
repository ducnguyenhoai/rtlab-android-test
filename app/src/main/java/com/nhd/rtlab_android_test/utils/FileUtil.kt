package com.nhd.rtlab_android_test.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


fun resolveContentUri(context: Context, uri: Uri): String {
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(
        uri,
        DocumentsContract.getTreeDocumentId(uri)
    )
    val documentCursor = context.contentResolver.query(documentUri, null, null, null, null)

    var str = ""

    while (documentCursor!!.moveToNext()) {
        str = documentCursor.getString(0)
        if (str.matches(Regex(".*:.*"))) break
    }

    documentCursor.close()

    val split = str.split(":")

    val base: File = if (split[0] == "primary") {
        Environment.getExternalStorageDirectory()
    } else {
        File("/storage/${split[0]}")
    }

    if (!base.isDirectory) {
        throw Exception("'$uri' cannot be resolved in a valid path")
    }
    return File(base, split[1]).canonicalPath
}

fun getXmlFromFile(file: File, returnValue: (String) -> Unit) {
    Thread {
        try {
            val inputStream = file.inputStream()
            val bufferReader = inputStream.bufferedReader()
            val result = bufferReader.readText()
            if (result != "") {
                returnValue(result)
            }

            bufferReader.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            returnValue("")
        }
    }.start()
}

fun getInstanceIDFromXmlFile(file: File, returnValue: (String) -> Unit) {
    var instanceID = ""
    try {
        getXmlFromFile(file) {
            val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser: XmlPullParser = factory.newPullParser()
            parser.setInput(StringReader(it))
            var eventType: Int = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "instanceID") {
                    instanceID = parser.nextText()
                    break
                }
                eventType = parser.next()
            }
            returnValue(instanceID)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        returnValue("")
    }
}

fun getXmlToView(file: File, returnValue: (List<String>) -> Unit) {
    var instanceID = ""
    try {
        Thread {
            val dbf = DocumentBuilderFactory.newInstance()
            val doc: Document = dbf.newDocumentBuilder().parse(file)
            doc.documentElement.normalize()

            val elements: NodeList = doc.getElementsByTagName("*")
            val list = getListElement(elements.item(0).nodeName, elements)
            for (item in list) {
                if (item.contains("instanceID")) {
                    instanceID = item.replace("<instanceID>", "").replace("</instanceID>", "")
                    break
                }
            }
            list.add(0, instanceID)
            returnValue(list)
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
        returnValue(mutableListOf())
    }
}

fun getListElement(root: String, elements: NodeList): MutableList<String> {
    val list: MutableList<String> = mutableListOf()
    val filtered: MutableList<Element> = mutableListOf()
    list.add("<$root>")
    for (i in 0 until elements.length) {
        val elem: Element = elements.item(i) as Element
        if (elem.parentNode.nodeName == root) {
            filtered.add(elem)
        }
    }
    for (i in 0 until filtered.size) {
        val elem: Element = filtered[i]
        when (elem.childNodes.length) {
            0 -> {
                list.add("<${elem.nodeName} />")
            }
            1 -> {
                list.add("<${elem.nodeName}>${elem.firstChild.nodeValue}</${elem.nodeName}>")
            }
            else -> {
                val child: NodeList = elem.getElementsByTagName("*")
                list.addAll(getListElement(elem.nodeName, child))
            }
        }
    }
    list.add("</$root>")
    return list
}

fun copyFile(file: File, outputFilePath: String, returnValue: (Boolean) -> Unit) {
    var inputStream: FileInputStream? = null
    var outputStream: FileOutputStream? = null
    try {
        inputStream = file.inputStream()
        outputStream = FileOutputStream(File(outputFilePath))
        val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
        var count: Int
        while (inputStream.read(bytes).also { count = it } != -1) {
            outputStream.write(bytes, 0, count)
        }
        returnValue(true)
    } catch (e: Exception) {
        e.printStackTrace()
        returnValue(false)
    } finally {
        inputStream?.close()
        outputStream?.close()
    }
}