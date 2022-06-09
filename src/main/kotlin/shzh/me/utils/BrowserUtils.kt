package shzh.me.utils

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

object BrowserUtils {
    private val driver: ChromeDriver

    init {
        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1980,960");
        driver = ChromeDriver(options)
    }

    fun getDriver(): ChromeDriver {
        return driver
    }
}