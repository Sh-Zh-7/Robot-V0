package shzh.me.utils

object MessageBuilder {
    private var message: String = ""

    fun text(content: String, newline: Boolean = true): MessageBuilder {
        this.message += content + if (newline) { "\n" } else { "" }
        return this
    }

    fun image(url: String): MessageBuilder {
        this.message += "[CQ:image,file=$url]\n"
        return this
    }

    fun reply(id: Int): MessageBuilder {
        this.message += "[CQ:reply,id=$id]"
        return this
    }

    fun content(): String {
        return this.message
    }
}