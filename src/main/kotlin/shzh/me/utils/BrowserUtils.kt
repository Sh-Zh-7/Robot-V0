package shzh.me.utils

import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URL

object BrowserUtils {
    private var driver: RemoteWebDriver

    init {
        val options = ChromeOptions()
        options.addArguments("--no-sandbox")
        options.addArguments("--disable-dev-shm-usage")
        options.addArguments("--headless")
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1024,768");
        driver = RemoteWebDriver(URL("http://selenium:4444/wd/hub"), options)
    }

    fun getDriver(): RemoteWebDriver {
        return driver
    }
}