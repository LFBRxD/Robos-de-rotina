package stepAdjuster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GeradorDeReport {

	static String login = "";
	static String password = "";
	private static WebDriver driver = null;

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
			String baseUrl = "https://app.x-celera.com/xcelera-app/secure/execution-plans/13405/manage-execution";

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
				// TODO: handle exception
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtUser))).sendKeys(login);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtPass))).sendKeys(password);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnLogin))).click();
			esperar(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(homeBrand)));
			driver.get(baseUrl);

			String lblFailed = "//td[.='Failed']/..";
			String btnCancel = "//button[.='Cancel']";
			String btnEditAction = "/..//td[5]/button[2][@title='See Logs']";
			String listaDeStrings = "//td[@title]";

			String xpathExpressionFailedButtonFilter = "//div[@class='btn-group btn-group-toggle']/label[contains(.,'Failed')]";

			String xpathTextFalha = "(//div[contains(@id,'action_')]/div/div/div/div/div[contains(@class,'bg-danger')]/ancestor::*[contains(@id,'action_')]/div/div[2]//p)[3]";
			String xpathProcedureTitle = "//div[contains(@id,'action_')]/div/div/div/div/div[contains(@class,'bg-danger')]/ancestor::*[contains(@id,'action_')]/div/div[2]//h6";

			List<String> currentList = new ArrayList<String>();
			List<String> totalList = new ArrayList<String>();

			Map<String, Integer> incidenciasDeFalha = new HashMap<String, Integer>();

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpressionFailedButtonFilter)));
//			esperar(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpressionFailedButtonFilter)))
					.click();

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(lblFailed)));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(listaDeStrings)));

			int pageId = 1;
			boolean doAgain = true;
			final WebDriverWait wait3 = new WebDriverWait(driver, 3);
			do {
				String currentPageSelected = "//li[contains(@class,'page-item') and contains(@class,'active')]/a/span[.='"
						+ (pageId++) + "']";
				String nextPageToSelect = "//li[contains(@class,'page-item')]/a/span[.='" + (pageId) + "']";

				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(currentPageSelected)));
				esperar(3);

				// logica para interar sobre cada um dos elementos

				List<WebElement> ownElements = driver.findElements(By.xpath(listaDeStrings));
				pageSCenaries(currentList, ownElements);
				WebElement last = null;
				try {
					System.out.println("\n\n");
					for (String webElement : currentList) {
						System.out.println(webElement);
						try {
							String xpathExpression = "//td[contains(@title,'" + webElement.replace("...", "") + "')]"
									+ btnEditAction;
							last = driver.findElement(By.xpath(xpathExpression));
							last.click();

							wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathProcedureTitle)));
							waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathTextFalha)));
							String titulo = driver.findElement(By.xpath(xpathProcedureTitle)).getText();
							String falha = driver.findElement(By.xpath(xpathTextFalha)).getText();

							System.out.println(titulo);
							if (incidenciasDeFalha.containsKey(titulo)) {
								incidenciasDeFalha.put(titulo, incidenciasDeFalha.get(titulo) + 1);
							} else {
								incidenciasDeFalha.put(titulo, 1);
							}

						} catch (TimeoutException ignore) {
						} catch (Exception e) {
						}

						// cancelar e ir pro proximo
						List<WebElement> lBtnCancel = driver.findElements(By.xpath(btnCancel));
						try {
							while (lBtnCancel.size() > 0) {
								try {
									waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnCancel)));
									lBtnCancel.forEach(e -> {
										e.click();
										try {
											Thread.sleep(150);
										} catch (InterruptedException e1) {
											e1.printStackTrace();
										}
									});
								} catch (Exception e) {
									lBtnCancel.get(lBtnCancel.size() - 1).click();
									lBtnCancel = driver.findElements(By.xpath(btnCancel));
								}
							}
						} catch (Exception e) {
						}

					}
				} catch (StaleElementReferenceException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}

				// check if has next page to iterate
				try {
					WebElement btnNextPage = wait3
							.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(nextPageToSelect)));
					btnNextPage.click();
//					waitf.until(ExpectedConditions.invisibilityOf(driver.findElement(By.xpath("//td[contains(@title,'"
//							+ currentList.get(currentList.size() - 1).replace("...", "") + "')]"))));
					waitf.until(ExpectedConditions.invisibilityOf(last));
				} catch (TimeoutException e) {
					doAgain = false;
				} catch (NoSuchElementException e) {
				} catch (IndexOutOfBoundsException e) {
					pageSCenaries(currentList, ownElements);
				} catch (Exception e) {
					e.printStackTrace();
				}

				currentList.forEach(cenario -> {
					if (!totalList.contains(cenario)) {
						totalList.add(cenario);
					}
				});

				System.out.println("total: " + totalList.size() + ", pagina atual: " + pageId);
				ownElements = driver.findElements(By.xpath(listaDeStrings));
				for (String key : incidenciasDeFalha.keySet()) {
					Integer value = incidenciasDeFalha.get(key);
					System.err.println("Falha: " + key + " incidencias " + value);
				}

			} while (doAgain);

			esperar(20);

			System.out.println("\n\n\n\n Finalizado!");
			for (String key : incidenciasDeFalha.keySet()) {
				Integer value = incidenciasDeFalha.get(key);
				System.out.println(
						"* " + value + " " + (value > 1 ? "cenários falhados por " : "cenário falhado por ") + key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.close();
		}

	}

	private static void pageSCenaries(List<String> currentList, List<WebElement> ownElements) {
		currentList.clear();
		esperar(1);
		try {
			for (WebElement webElement : ownElements) {
				String cen = webElement.getAttribute("innerHTML").trim();
				currentList.add(cen);
			}
		} catch (Exception e) {
		}
	}

	private static void esperar(int sec) {
		try {
			Thread.sleep(sec * 1000);
		} catch (Exception ignorar) {
		}
	}

	private static void startDriver() {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + File.separator + "DriverSelenium"
				+ File.separator + "chromedriver.exe");
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
		driver.manage().deleteAllCookies();
	}

}
