package stepAdjuster;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class EqualizadorDeConfigurador {
	private static WebDriver driver = null;

	// configuradores da classe
	private static Boolean sequencial = true;
//	final static String executionConfiguration = " Selenium + Chrome + Locator 2 ";
	final static String executionConfiguration = " (4.0) Selenium + Chrome + Locator 2 ";

	static String login = "";
	static String password = "";
	final static String computerList[] = { "HDIWUFTAPP01 ", "HDIWUFTAPP01A", "HDIWUFTAPP01B", "HDIWUFTAPP01C",
			"HDIWUFTAPP01D", "HDIWUFTAPP02 ", "HDIWUFTAPP02A", "HDIWUFTAPP02B", "HDIWUFTAPP02C", "HDIWUFTAPP02D",
			"HDIWUFTAPP03 ", "HDIWUFTAPP03A", "HDIWUFTAPP03B", "HDIWUFTAPP03C", "HDIWUFTAPP03D", "HDIWUFTAPP04 ",
			"HDIWUFTAPP04A", "HDIWUFTAPP04B", "HDIWUFTAPP04C", "HDIWUFTAPP04D", "HDIWUFTAPP05 ", "HDIWUFTAPP05A",
			"HDIWUFTAPP05B", "HDIWUFTAPP05C", "HDIWUFTAPP05D", "HDIWUFTAPP06 ", "HDIWUFTAPP06A", "HDIWUFTAPP06B",
			"HDIWUFTAPP06C", "HDIWUFTAPP06D", "HDIWUFTAPP07 ", "HDIWUFTAPP07A", "HDIWUFTAPP07B", "HDIWUFTAPP07C",
			"HDIWUFTAPP07D", "HDIWUFTAPP08 ", "HDIWUFTAPP08A", "HDIWUFTAPP08B", "HDIWUFTAPP08C", "HDIWUFTAPP08D" };

	public static Properties getProp() throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream("./configs/config.properties");
		props.load(file);
		return props;

	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException {
		Properties prop;
		try {
			prop = getProp();
			login = prop.getProperty("app.login");
			password = prop.getProperty("app.senha");
		} catch (IOException e2) {
		}
		System.setProperty("webdriver.gecko.driver", "C:\\geckodriver.exe");
		startDriver();
		driver.manage().window().maximize();
		try {
			WebDriverWait wait = new WebDriverWait(driver, 60);
			WebDriverWait waitf = new WebDriverWait(driver, 10);
			String baseUrl = "https://app.x-celera.com/xcelera-app/secure/execution-plans/1857";
			String baseUrlPara = "https://app.x-celera.com/xcelera-app/secure/execution-plans/13298";

			// telaLogin
			String txtUser = "//*[@id=\"Username\"]";
			String txtPass = "//*[@id=\"Password\"]";
			String btnLogin = "//button[.='Login']";
			String homeBrand = "//img[@alt='Xcelera - Home']";

			driver.get(baseUrl);
			try {
				wait.until(ExpectedConditions.alertIsPresent());
				driver.switchTo().alert().dismiss();
			} catch (Exception e) {
			}

			esperar(1);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtUser))).sendKeys(login);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtPass))).sendKeys(password);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnLogin))).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(homeBrand)));
			driver.get(baseUrl);

			String btnConfigExecution = "//a[.='Configure Execution']";
			String cbbComputers = "//div[.=' Test Case ']/../../td/select[@id='Worker']";
			String cbbExecutionConfigItems = "//div[.=' Test Case ']/../../td/select[@id='ExecutionConfigItems']";

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnConfigExecution))).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(cbbComputers)));

			Map<String, String> cbbConfigSettings = new HashMap<>();

			List<WebElement> listCbbComputers = driver.findElements(By.xpath(cbbComputers));
			List<WebElement> listCbbExecutionConfig = driver.findElements(By.xpath(cbbExecutionConfigItems));

			List<WebElement> listCenarios = driver.findElements(By.xpath("//div[.=' Test Case ']/../self::td[@title]"));
			for (WebElement currRow : listCenarios) {
				String titulo = currRow.getAttribute("title");
				String xpathCbb = "//td[@title='" + titulo + "']/../td/select[@id='ExecutionConfigItems']";
				WebElement cbbCenario = driver.findElement(By.xpath(xpathCbb));
				final Select selectBox = new Select(cbbCenario);
				cbbConfigSettings.put(titulo, selectBox.getFirstSelectedOption().getText());
			}

			driver.get(baseUrlPara);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnConfigExecution))).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(cbbComputers)));
			int I = 0;
			List<WebElement> listCenariosPara = driver
					.findElements(By.xpath("//div[.=' Test Case ']/../self::td[@title]"));
			for (WebElement currRow : listCenariosPara) {
				String titulo = currRow.getAttribute("title");
				String xpathCbb = "//td[@title='" + titulo + "']/../td/select[@id='ExecutionConfigItems']";
				WebElement cbbCenario = driver.findElement(By.xpath(xpathCbb));
				final Select selectBox = new Select(cbbCenario);
				if (cbbConfigSettings.get(titulo) != null) {
					if (cbbConfigSettings.get(titulo).equalsIgnoreCase("[Intra][PROD] Selenium + Chrome + Locator2")) {
						selectBox.selectByVisibleText("[Intra][PROD] Selenium + Chrome + Locator2");
						System.out.println("Trocou do: " + titulo + " " + (++I));
					}
				}
			}

			String btnSave = "//button[.='Computer List']/../button[ @type='submit' and .='Save']";
			WebElement objBtnSave = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnSave)));
			objBtnSave.click();
			wait.until(ExpectedConditions.invisibilityOf(objBtnSave));

		} finally {
			driver.close();
		}

	}

	private static void esperar(int sec) {
		try {
			System.out.println("aguardando " + sec + " segundos");
			Thread.sleep(sec * 1000);
		} catch (Exception ignorar) {
			ignorar.printStackTrace();
		}
	}

	private static void startDriver() {
		WebDriverManager.chromedriver().setup();
		System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
		Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
		Logger.getLogger("org.slf4j.impl.StaticLoggerBinder").setLevel(Level.OFF);
		ChromeOptions chromeOptions = new ChromeOptions();
		// chromeOptions.addArguments("--user-data-dir=C:/Users/grupohdi01/AppData/Local/Google/Chrome/User
		// Data");
		// chromeOptions.addArguments("--profile-directory=Default");
		chromeOptions.addArguments("--lang=pt");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-web-security");
		chromeOptions.addArguments("disable-infobars");
		chromeOptions.addArguments("--window-size=1920,1080");
//		chromeOptions.addArguments("--headless");
		chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
//		chromeOptions.addExtensions(new File(System.getProperty("user.dir") + File.separator + "Extensions"
//				+ File.separator + "extension_2_3_164_0.crx"));

		driver = new ChromeDriver(chromeOptions);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().deleteAllCookies();
	}

}
