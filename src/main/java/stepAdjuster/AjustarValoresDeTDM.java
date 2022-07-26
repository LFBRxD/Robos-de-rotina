package stepAdjuster;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AjustarValoresDeTDM {

	static String login = "";
	static String password = "";

	static String tituloDoPasso = null;
	static Map<String, String> dadosPasso = new HashMap<>();
	private static WebDriver driver = null;

	public static Properties getProp() throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream("./configs/config.properties");
		props.load(file);
		return props;

	}

	/**
	 * Cria a lista de elementos para ajustar
	 *
	 */
	private static void createListOfElementsToEdit() {
		// passo para editar
		tituloDoPasso = "Clicar no Link Usu�rio";

		// campos para editar
		dadosPasso.put("GRD_Corretores", "#c.intranetUser");
//		dadosPasso.put("CBO_COD_ATIVIDADE RISCO", "index;1");
//		dadosPasso.put("CBO_CONSTRU��O", "index;1");

//		// passo para editar
//		tituloDoPasso = "Preencher Dados do Produto";
//
//		// campos para editar
//		dadosPasso.put("CBO_PRODUTO", "index;1");
//		dadosPasso.put("TXT_INICIO_VIGENCIA", "<IGNORE>");
//		dadosPasso.put("TXT_FIM_VIGENCIA", "<IGNORE>");
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException {

		createListOfElementsToEdit();

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
			WebDriverWait wait = new WebDriverWait(driver, 30);
			WebDriverWait waitf = new WebDriverWait(driver, 10);
			String baseUrl = "https://app.x-celera.com/xcelera-app/secure/execution-plans/13298/manage-execution";

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

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtUser))).sendKeys(login);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(txtPass))).sendKeys(password);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnLogin))).click();
			esperar(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(homeBrand)));
			driver.get(baseUrl);

//			String lblCollunmStatus = "//td[.='Failed' or .='Failed']/..";
			String btnCancel = "//button[.='Cancel']";
			String btnEditAction = "/..//td[5]/button[1][@title='View Action']";
			String listaDeStrings = "//td[@title]";

			String xpathExpressionAllButtonFilter = "//div[@class='btn-group btn-group-toggle']/label[contains(.,'All')]";

			List<String> currentList = new ArrayList<>();
			List<String> totalList = new ArrayList<>();

			Map<String, Integer> incidenciasDeFalha = new HashMap<>();

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpressionAllButtonFilter)));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpressionAllButtonFilter))).click();

//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(lblCollunmStatus)));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(listaDeStrings)));

			int pageId = 1;
			boolean doAgain = true;
			final WebDriverWait wait3 = new WebDriverWait(driver, 3);
			do {
				String currentPageSelected = "//li[contains(@class,'page-item') and contains(@class,'active')]/a/span[.='"
						+ (pageId++) + "']";
				String nextPageToSelect = "//li[contains(@class,'page-item')]/a/span[.='" + (pageId) + "']";

				try {
					esperar(3);
					waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(currentPageSelected)));
				} catch (ElementClickInterceptedException e) {
					e.printStackTrace();
					esperar(60);
				} catch (Exception e) {
				}

				// logica para interar sobre cada um dos elementos

				List<WebElement> ownElements = driver.findElements(By.xpath(listaDeStrings));
				pageSCenaries(currentList, ownElements);
				WebElement last = null;
				try {
					System.out.println("\n\n");
					for (String webElement : currentList) {
						performCancell(waitf, btnCancel);
						try {
							String xpathExpression = "//td[contains(@title,'" + webElement.replace("...", "") + "')]"
									+ btnEditAction;
							last = driver.findElement(By.xpath(xpathExpression));

							last.click();

							final String xpathTituloEdit = "//h1[contains(.,'Test Case Actions')]";
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathTituloEdit)));

							final String xpathButtonEdit = "//div/span[contains(.,'" + tituloDoPasso
									+ "')]/../../div[2]/div[2]/button[1]";
							final String xpathTituloValueTDMEdit = "//h1[contains(.,'Values of Action from ')]";
//							waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathButtonEdit)))
//									.click();

							driver.findElement(By.xpath(xpathButtonEdit)).click();

							System.out.println("Editando " + webElement);
							wait.until(
									ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathTituloValueTDMEdit)));

							for (Entry<String, String> entry : dadosPasso.entrySet()) {

								final String xpathTdParaEditar = "//thead/tr/th/div/span[.='" + entry.getKey()
										+ "']/ancestor::table/tbody/tr/td[contains(@class,'highlight')]";

								final String inputTDMValue = "//div[not(@display='none')]/textarea[@class='handsontableInput']";

								Actions action = new Actions(driver);

								final String xpathThTable = "//div[contains(@class,'ht_master')]//table/thead/tr/th/div/span";

								final List<WebElement> sda = driver.findElements(By.xpath(xpathThTable));
								sda.forEach(e -> {
									if (e.getAttribute("innerHTML").contains(entry.getKey())) {
										String localXpath = "//span[.='" + entry.getKey()
												+ "']/ancestor::table/tbody/tr/td[" + sda.indexOf(e) + "]";
										WebElement td = wait
												.until(ExpectedConditions.elementToBeClickable(By.xpath(localXpath)));

										if (!td.getText().equalsIgnoreCase(entry.getValue())) {
											td.click();
											action.moveToElement(td).moveToElement(td).click().build().perform();

											wait.until(ExpectedConditions
													.visibilityOfElementLocated(By.xpath(xpathTdParaEditar)));
											WebElement we = driver.findElement(By.xpath(xpathTdParaEditar));
											action.moveToElement(we)
													.moveToElement(driver.findElement(By.xpath(xpathTdParaEditar)))
													.doubleClick().build().perform();

											WebElement txtElement = wait.until(ExpectedConditions
													.visibilityOfElementLocated(By.xpath(inputTDMValue)));

											txtElement.clear();
											txtElement.sendKeys(entry.getValue());
										}
									}
								});

							}

							final String btnSalvarValorTDM = "//button[.='Import']/../button[.='Save']";

							wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnSalvarValorTDM)))
									.click();

							final String msgRegisterUpdatedSuccessfully = "//*[contains(.,'Register updated successfully')]";
							wait.until(ExpectedConditions
									.visibilityOfElementLocated(By.xpath(msgRegisterUpdatedSuccessfully)));
						} catch (TimeoutException ignore) {
						} catch (NoSuchElementException ignore) {
//							ignore.printStackTrace();
						} catch (Exception e) {
							throw e;
						}

					}
				} catch (StaleElementReferenceException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}

				// check if has next page to iterate
				try {
					performCancell(waitf, btnCancel);
					WebElement btnNextPage = wait3
							.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(nextPageToSelect)));
					btnNextPage.click();
					waitf.until(ExpectedConditions.invisibilityOf(last));
				} catch (TimeoutException e) {
					doAgain = false;
				} catch (NoSuchElementException e) {
				} catch (IndexOutOfBoundsException e) {
					pageSCenaries(currentList, ownElements);
				} catch (NullPointerException e) {
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
						"* " + value + " " + (value > 1 ? "cen�rios falhados por " : "cen�rio falhado por ") + key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.close();
		}

	}

	private static void performCancell(WebDriverWait waitf, String btnCancel) {
		try {
//			while (lBtnCancel.size() > 0) {
			while (waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnCancel))).isDisplayed()) {
				try {
					driver.findElement(By.xpath(btnCancel)).click();
//					waitf.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(btnCancel))).click();
				} catch (TimeoutException ignore) {
					System.out.println("lancou uma  TimeoutException no cancel");
				} catch (NoSuchElementException ignore) {
					System.out.println("lancou uma  NoSuchElementException no cancel");
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
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
		WebDriverManager.chromedriver().setup();
		System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
		Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
		Logger.getLogger("org.slf4j.impl.StaticLoggerBinder").setLevel(Level.OFF);
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--lang=pt");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-web-security");
		chromeOptions.addArguments("disable-infobars");
//		chromeOptions.addArguments("--window-size=1920,1080");
//		chromeOptions.addArguments("--headless");
		chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
		driver = new ChromeDriver(chromeOptions);
		driver.manage().deleteAllCookies();
	}

}
