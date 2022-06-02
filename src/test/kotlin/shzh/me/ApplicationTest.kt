package shzh.me

import io.ktor.server.testing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
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
        driver.get("https://t.bilibili.com/667051804590080023")

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
}