package stepAdjuster;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddStep {

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
			String baseUrl = "https://app.x-celera.com/xcelera-app/secure/execution-plans/2306/manage-execution";

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
			System.out.println();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtUser))).sendKeys(login);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtPass))).sendKeys(password);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnLogin))).click();
			esperar(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(homeBrand)));
			driver.get(baseUrl);

			String lblFailed = "//td[.='Passed']/..";
			String btnCancel = "//button[.='Cancel']";
			String listaDeStrings = "//td[@title]";
			String xpathExpressionFailedButtonFilter = "//div[@class='btn-group btn-group-toggle']/label[contains(.,'All')]";

			String xpathBtnNext = "//li[contains(@class,'page-item')]/a/span[.='Next']/..";

			List<String> currentList = new ArrayList<>();
			List<String> totalList = new ArrayList<>();

			Map<String, Integer> incidenciasDeFalha = new HashMap<>();

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpressionFailedButtonFilter)));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpressionFailedButtonFilter)))
					.click();

//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(lblFailed)));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(listaDeStrings)));

			List<WebElement> ownElements = driver.findElements(By.xpath(listaDeStrings));

			int loop = 0;
			while (loop++ < 100) {
				pageSCenaries(currentList, ownElements);
				try {
					for (String webElement : currentList) {
						if (totalList.contains(webElement)) {
							System.out.println("contem: " + webElement);

							driver.findElement(By.xpath("//td[@title='" + webElement
									+ "']/../td[contains(@class,'text-right')]/button[@title='Rerun Test Case']"))
									.click();
						} else {
							try {
								// Clicar em editar o cenário
//								String xpathExpression = "//td[contains(@title,'" + webElement.replace("...", "")
//										+ "')]" + btnEditAction;
//								driver.findElement(By.xpath(xpathExpression)).click();

								String xpathAction = "//button[@title='View Action']";
								driver.findElement(By.xpath(xpathAction)).click();

								String tituloTestCase = "//h1[.='Test Case Actions ']";
								waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tituloTestCase)));

								// Tela test case
								String tituloRepositoryType = "//label[.='Repository Type']/../select";
								WebElement cbb1 = waitf.until(
										ExpectedConditions.visibilityOfElementLocated(By.xpath(tituloRepositoryType)));
								selectCbbIndex(cbb1, "Web/Windows Forms");

								String tituloSystem = "//label[.='System']/../select";
								WebElement cbb2 = waitf
										.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tituloSystem)));
								selectCbbIndex(cbb2, "New HDI Digital ");

								String tituloScreen = "//label[.='Screen']/../select";
								WebElement cbb3 = waitf
										.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tituloScreen)));
								selectCbbIndex(cbb3, "Tela Modal");

								String tituloActionType = "//label[.='Action Type']/../select";
								WebElement cbb4 = waitf.until(
										ExpectedConditions.visibilityOfElementLocated(By.xpath(tituloActionType)));
								selectCbbIndex(cbb4, "Interaction");

								// step para drag
								String lblStepToDrag = "//li[@title='Realizar o fechamento das modais que aparecer']";

								WebElement sourceElement = wait
										.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(lblStepToDrag)));
								List<WebElement> elements = driver.findElements(By.xpath(lblStepToDrag));

								if (elements.size() < 2) {
									System.out.println(webElement);
									Robot bot = new Robot();
									Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
									System.out.println("Realizando dragnDrop");

									// step para arrastar depois
									String lblStepAfter = "//span[.='Login - Clicar botão Acessar']/../../..";

									Actions action = new Actions(driver);
//									action.dragAndDrop(sourceElement, sourceElement).build().perform();
//									action.clickAndHold(sourceElement).perform();
									sourceElement = wait.until(
											ExpectedConditions.visibilityOfElementLocated(By.xpath(lblStepToDrag)));
									System.out.println("Source element: x->" + sourceElement.getLocation().x + " y->"
											+ sourceElement.getLocation().y);

//
									esperar(2);
									WebElement destElement = wait.until(
											ExpectedConditions.visibilityOfElementLocated(By.xpath(lblStepAfter)));
									System.out.println("Dest element: x->" + destElement.getLocation().x + " y->"
											+ destElement.getLocation().y);
//									action.moveToElement(destElement).pause(1)
//											.moveByOffset(destElement.getLocation().x + 100,
//													destElement.getLocation().y + 160)
//											.click().build().perform();

									bot.mouseMove(100, 800);
									esperar(2);
									bot.mouseMove(900, 1000);
									esperar(2);
									bot.mouseMove( 500,  500);
									esperar(2);
								} else {
									// cancelar e ir pro proximo
									List<WebElement> lBtnCancel = driver.findElements(By.xpath(btnCancel));
									try {
										while (lBtnCancel.size() > 0) {
											try {
												waitf.until(ExpectedConditions
														.visibilityOfElementLocated(By.xpath(btnCancel)));
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

//								String btnSalvar = "//button[.='Save']";
//								wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnSalvar))).click();

								System.out.println("Next");

							} catch (TimeoutException ignore) {
							} catch (Exception e) {
							}

							String tituloTestCase = "//h1[.='Test Case Actions ']";
							wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(tituloTestCase)));

							String xpathAction = "//button[@title='Rerun Test Case']";
							driver.findElement(By.xpath(xpathAction)).click();

//
						}
					}
				} catch (StaleElementReferenceException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
//					waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathBtnNext))).click();
				} catch (TimeoutException e) {
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					waitf.until(ExpectedConditions.invisibilityOf(driver.findElement(By.xpath("//td[contains(@title,'"
							+ currentList.get(currentList.size() - 1).replace("...", "") + "')]"))));
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

				System.out.println("total: " + totalList.size());
				ownElements = driver.findElements(By.xpath(listaDeStrings));
				for (String key : incidenciasDeFalha.keySet()) {
					Integer value = incidenciasDeFalha.get(key);
					System.err.println("Falha: " + key + " incidencias " + value);
				}
			}

			esperar(20);

		} finally {
			driver.close();
		}

	}

	private static void selectCbbIndex(WebElement webElement, String valorSelect) {
		final Select selectBox = new Select(webElement);
		selectBox.selectByVisibleText(valorSelect.trim());
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
			System.out.println("aguardando");
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
		chromeOptions.addArguments("--lang=pt");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-web-security");
		chromeOptions.addArguments("disable-infobars");
		chromeOptions.addArguments("--window-size=1920,1080");
//		chromeOptions.addArguments("--headless");
		chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);

		driver = new ChromeDriver(chromeOptions);
		driver.manage().deleteAllCookies();
	}

}
