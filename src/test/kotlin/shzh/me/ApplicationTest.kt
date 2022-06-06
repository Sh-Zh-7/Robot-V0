package shzh.me

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.ktor.server.testing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.Test


class ApplicationTest {
    @Test
    fun testScreenshot() = testApplication {
        // Headless mode for server use
        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1980,960");
        val driver = ChromeDriver(options)

        // Switch to Bilibili dynamic page
        driver.get("https://t.bilibili.com/667944337435263017")

        // Hide non-login users popup
        val popup = driver.findElement(By.cssSelector("div.unlogin-popover.unlogin-popover-avatar"))
        driver.executeScript("arguments[0].style.display = 'none';", popup)

        // Element to screenshot
        val target = driver.findElement(By.cssSelector("#app > div > div.detail-content > div > div > div"))
        val screenshot = target.getScreenshotAs(OutputType.BYTES)

        // Clip main content
        val destImage: BufferedImage
        val content = driver.findElement(By.cssSelector("#app > div > div.detail-content > div > div > div > div.main-content"))
        withContext(Dispatchers.IO) {
            val bufferedImage = ImageIO.read(screenshot.inputStream())
            destImage = bufferedImage.getSubimage(0, 0, target.size.width, content.size.height)

            // Save image file
            val filename = UUID.randomUUID().toString()
            val file = File("./$filename.png")
            ImageIO.write(destImage, "png", file)
        }

        driver.quit()
    }

    @Test
    fun testRSS() {
        val url = "https://rsshub.app/bilibili/user/dynamic/359345207"
        val feed: SyndFeed = SyndFeedInput().build(XmlReader(URL(url)))
        println(feed.entries[0].publishedDate)
    }

    @Test
    fun testHTMLToImage() = testApplication {
        // Headless mode for server use
        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1980,960");
        val driver = ChromeDriver(options)

        // Switch to Bilibili dynamic page
        driver.get("file:///Users/shzh7/IdeaProjects/Robot-V0/index.html")
        val target = driver.findElement(By.cssSelector(".container"))
        val screenshot = target.getScreenshotAs(OutputType.FILE)
        withContext(Dispatchers.IO) {
            val image = ImageIO.read(screenshot.inputStream())

            val filename = UUID.randomUUID().toString()
            val file = File("./$filename.png")
            ImageIO.write(image, "png", file)
        }

        driver.quit()
    }

}