package shzh.me.utils

object MessageUtils {
    private lateinit var message: String

    fun builder(): MessageUtils {
        this.message = ""
        return this
    }

    fun text(content: String, newline: Boolean = true): MessageUtils {
        this.message += content + if (newline) { "\n" } else { "" }
        return this
    }

    fun image(url: String): MessageUtils {
        this.message += "[CQ:image,file=$url]\n"
        return this
    }

    fun reply(id: Int): MessageUtils {
        this.message += "[CQ:reply,id=$id]"
        return this
    }

    fun music(type: String, id: Long): MessageUtils {
        this.message += "[CQ:music,type=$type,id=$id]"
        return this
    }

    fun content(): String {
        return this.message
    }
}