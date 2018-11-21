
package mpos.pdfXmlGenerator;

//import cn.com.n22.global.common.utils.PropertyUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import com.itextpdf.text.pdf.util.SmartPdfSplitter;
import org.apache.commons.io.FileUtils;
import org.owasp.esapi.ESAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import bsh.This;

//

/**
 * Created by Kal on 2017/3/2.
 */
public class DocGenerator {

	public DocGenerator(String fontsPath, String dataJsonPath, String webPackagePath, String pdfSavePath) {
		this.mainPath = pdfSavePath;
		this.resourcePath = webPackagePath;
		this.jsonPath = webPackagePath;
		this.normalFontPath = fontsPath + "\\kaiu.ttf";
		this.symbolFontPath = fontsPath + "\\DejaVuSans.ttf";
	}

	// 主要目錄
	private static String mainPath = "C:\\Users\\Weu\\Desktop\\DocGenerate\\pdfTest\\";

	// 文件版本
	private static String docVersion;

	// 設定檔目錄
	// private static String resourcePath = "C:\\Users\\Weu\\Desktop\\mPOS_iOSHtml";
	private static String resourcePath = "C:\\Users\\Weu\\Desktop\\test";

	// 產生 pdf 所需的 json data
	private static String jsonPath = "C:\\Users\\Weu\\Desktop\\DocGenerate\\data.json";

	// 處理頁數
	private int pageNum = 0;
	// 簽名檔list
	// public List<SignatureData> signatureDataList = new
	// ArrayList<SignatureData>();
	// uiJson所需頁數
	public int uiJsonPageNum = 0;

	// 一般文字字型位置+大小
	static String normalFontPath = "C:\\Users\\Weu\\Desktop\\DocGenerate\\pdfTest\\kaiu.ttf";
	static float kaiuFontSize = 8;

	// 特殊符號字型位置+大小
	static String symbolFontPath = "C:\\Users\\Weu\\Desktop\\DocGenerate\\pdfTest\\DejaVuSans.ttf";
	static float dejaVuFontSize = 15;

	// 問卷Map
	// private Map<String, List<JsonNode>> questMap = new LinkedHashMap<>();

	public boolean startGenerate(String fillData, String newPolicyNo) throws IOException {

		File index = new File(mainPath + newPolicyNo);
		String[] entries = index.list();
		for (String s : entries) {
			File currentFile = new File(index.getPath(), s);
			currentFile.delete();
		}

		// FileUtils.deleteDirectory(new File(mainPath + newPolicyNo));
		return startGenerate(fillData, newPolicyNo, null);
	}

	public boolean startGenerate(String fillData, String newPolicyNo, String subPath) {

		try {

			// 檔案路徑
			if (Files.notExists(Paths.get(mainPath + newPolicyNo))) {
				Files.createDirectories(Paths.get(mainPath + newPolicyNo));
			} else if (!Files.notExists(Paths.get(mainPath + newPolicyNo)) && subPath != null) { // 刪除特定主約 重新套印
				FileUtils.deleteDirectory(new File(mainPath + newPolicyNo + "/" + subPath));
			} else if (!Files.notExists(Paths.get(mainPath + newPolicyNo)) && subPath == null) { // 刪除全部主約 重新套印
				for (int i = 0; i < 3; i++) {
					try {
						FileUtils.deleteDirectory(new File(mainPath + newPolicyNo + "/" + i));
					} catch (Exception e) {
						System.out.println("刪除:" + mainPath + newPolicyNo + "/" + i + "時發生錯誤");
					}
				}
			} else {
				System.out.println("Mobile Insured Error: File directory already exist.");
				return false;
			}

			// boolean containProposal = splitProposal(fillData, newPolicyNo);
			java.util.List<String> filePath = createPdf(fillData, newPolicyNo, subPath); // 產生PDF
			// createQuestionaire(fillData, newPolicyNo, subPath);
			// createSignaturePdf(filePath);
			// if (containProposal) { // 刪除建議書
			// FileUtils.deleteDirectory(new File(mainPath + newPolicyNo + "/proposal"));
			// }
			if (filePath != null && uiJsonPageNum <= filePath.size()) {
				return true;
			} else {
				// 缺頁時刪除要保文件
				FileUtils.deleteDirectory(new File(mainPath + newPolicyNo));
				System.out.println("Mobile Insured Error: Fail when generating pdf or tif.");
				// 需要頁數,套印頁數
				System.out.println(
						"Needed page :" + uiJsonPageNum + ",generated page:" + filePath == null ? 0 : filePath.size());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/* 根據jsonData+xml設定檔產生PDF並回傳 */
	private java.util.List<String> createPdf(String jsonData, String newPolicyNo, String subPath) {

		try {
			/* 字體設定 */
			/* 一般字型 */
			BaseFont normalFont = BaseFont.createFont(normalFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			/* 符號字型 */
			BaseFont symbolFont = BaseFont.createFont(symbolFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			/* json2map */
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jmap = mapper.readTree(jsonData); // 將json各值放入map
			/* 取得文件分組數 (主約數) */
			int groupNum = jmap.at("/page").size();
			// int groupOffset = 0; // 偏移量 如果是多主約(>1) 編號規則從1開始 若否 則為0
			// if(groupNum>1){
			// groupOffset =1;
			// }
			/* 路徑LIST */
			java.util.List<String> pathList = new ArrayList<>();
			for (int g = 0; g < groupNum; g++) {
				/* 取得頁數 */
				int pSize = jmap.at("/page/" + g).size();
				uiJsonPageNum += pSize;
				/* 開始處理 */
				for (int p = 0; p < pSize; p++) {
					/* 生成路徑Map + 產生路徑資料夾 */
					HashMap<String, String> pathMap = createPathMap(jmap, p, newPolicyNo, g, subPath);
					/* 路徑Map為Null時處理下一頁 */
					if (pathMap == null || pathMap.size() == 0) {
						continue;
					}

					/* PDF設定 */
					PdfReader reader = new PdfReader(pathMap.get("original_PDF_path")); // input PDF path
					/* 設定檔內容為空或找不到設定檔 直接存PDF 處理下一頁 */

					for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++) {
						// SignatureData signatureData = new SignatureData();
						/* 取得xml items */
						NodeList items = getNodeList(pathMap.get("setting_xml_path"));
						if (Files.notExists(Paths.get(mainPath + newPolicyNo + "/"
								+ (subPath == null ? g : Integer.valueOf(subPath)) + "/" + this.pageNum))) {
							Files.createDirectories(Paths.get(mainPath + newPolicyNo + "/"
									+ (subPath == null ? g : Integer.valueOf(subPath)) + "/" + this.pageNum));
						}
						this.pageNum++;
						if (items == null) { // 若不須套印 不做下面動作
							// PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(
							// ESAPI.encoder().canonicalize(pathMap.get("destination_PDF_path")))); //
							// output PDF
							PdfStamper stamper = new PdfStamper(reader,
									new FileOutputStream(pathMap.get("destination_PDF_path"))); // output PDF
							// path
							stamper.close();
							reader.close();
							pathList.add(pathMap.get("destination_PDF_path"));
							// pageNum++;
							createPathMap(jmap, p, newPolicyNo, g, subPath);
							continue;
						}
						Set<String> set = new HashSet<String>();
						Element ele = ((Element) items.item(5));
						if (ele != null && ele.getAttribute("menuId").equals("questionnaire")) {
							// String barcode = jmap.at("/page/" + g + "/" + p).asText();
							// List<JsonNode> questList = findQuestWithBarCode(barcode, jmap);
							// questMap.put(jmap.at("/page/" + g + "/" + p).asText(), questList);
							// this.pageNum--;
							continue;
						} else {
							set.add("");
						}

						for (String s : set) { // s = 問卷json中間層字串

							// output PDF path
							// PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(
							// ESAPI.encoder().canonicalize(pathMap.get("destination_PDF_path"))));
							PdfStamper stamper = new PdfStamper(reader,
									new FileOutputStream(pathMap.get("destination_PDF_path")));

							for (int i = 0; i < items.getLength(); i++) {
								Node item = items.item(i);
								Element itemElement = (Element) item;

								PdfContentByte canvas = stamper.getOverContent(pageNumber); // 第pageNumber頁內容
																							// 目前都為一份pdf一頁
								canvas.beginText();
								float x = 0;
								float y = 0;
								if (itemElement.getAttribute("x") != null && itemElement.getAttribute("y") != null
										&& !itemElement.getAttribute("x").equals("")
										&& !itemElement.getAttribute("y").equals("")) {

									if (itemElement.getAttribute("signatureName") != null
											&& !itemElement.getAttribute("signatureName").equals("")) {
										// String fieldContent = jmap
										// .at("/signature/" + s + itemElement.getAttribute("itemId"))
										// .asText();
										// /*BASE64編碼*/
										//// com.itextpdf.text.Image sigImage = stringDecodeToImage(fieldContent);
										//// /*此段加入簽名檔data 之後產生簽名檔*/
										//// if (signatureData.getDataMap().get("docName") == null) {
										//// File xml = new
										// File(ESAPI.encoder().canonicalize(pathMap.get("setting_xml_path"))); //xml
										// path
										//// DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
										//// DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
										//// Document doc = dBuilder.parse(xml);
										//// doc.getDocumentElement().normalize();
										//// NodeList titleItems = doc.getElementsByTagName("title");
										//// Node title = titleItems.item(0);
										//// Element titleElement = (Element) title;
										//// if(titleElement.getAttribute("name").contains("財務狀況告知書")){
										//// signatureData.getDataMap().put("docName", "財務狀況告知書");
										//// }else{
										//// signatureData.getDataMap().put("docName",
										// titleElement.getAttribute("name"));
										//// }
										//// signatureData.getDataMap().put("path",
										// pathMap.get("destination_PDF_path"));
										//// signatureData.getDataMap()
										//// .put("numbering", newPolicyNo);
										//// }
										// String key = itemElement.getAttribute("signatureId");
										// String name = "";
										// if(!jmap.at("/signature/" + key).asText().isEmpty()) {
										// name = "(" + jmap.at("/signature/" + key).asText() + ")";
										// }
										// signatureData.getDataMap().put(key + "Name", name); //加入姓名到簽名檔nameMap
										// signatureData.getSigMap().put(key, fieldContent); //加入圖檔到簽名檔sigMap
										// /*簽名圖檔Decode失敗 直接回傳null*/
										// if (sigImage == null) {
										//// stamper.close();
										//// return null;
										// canvas.endText();
										// continue;
										// }
										// setImgSizeAndPos(sigImage, itemElement, canvas); //設定圖片位置及大小
										// canvas.setTextMatrix(0, 0);
										// canvas.setFontAndSize(normalFont, kaiuFontSize);
										// try {
										// canvas.addImage(sigImage);
										// }catch (Exception e){
										// System.out.println("要保書套印簽名發生錯誤 檔案:"+pathMap.get("destination_PDF_path"));
										// System.out.println(e.getMessage());
										// }

									} else if (jmap
											.at("/" + itemElement.getAttribute("menuId") + "/" + s
													+ itemElement.getAttribute("itemId")) // 打勾字型及位置設定
											.asText().equals("✔")) {
										canvas.setFontAndSize(symbolFont, dejaVuFontSize); // set font and size
										x = getX(jmap, itemElement, symbolFont, canvas, s);
										y = getY(jmap, itemElement, symbolFont, canvas, s);
									} else { // 其他字型及位置設定
										canvas.setFontAndSize(normalFont, kaiuFontSize); // set font and size
										x = getX(jmap, itemElement, normalFont, canvas, s);
										y = getY(jmap, itemElement, normalFont, canvas, s);
									}
									String content = jmap.at("/" + itemElement.getAttribute("menuId") + "/" + s
											+ itemElement.getAttribute("itemId")).asText(); // json value 取得
									canvas.setRGBColorFill(0, 0, 80); // 顏色更改
									if (itemElement.getAttribute("wrapLine") != null
											&& !itemElement.getAttribute("wrapLine").equals("")) { // 換行
										int offset = 1;
										if (content.matches("^\\w+$")) { // 若字串皆為英文或數字 換行字數*2
											offset = 2;
										}
										content = contentWrap(content,
												Integer.parseInt(itemElement.getAttribute("wrapLine")) * offset);
										System.out.println(content);
										String[] contentArray = content.split("\n");
										canvas.endText();
										for (int j = 0; j < contentArray.length; j++) {
											canvas.beginText();
											if (itemElement.getAttribute("horAlign").equals("right")) { // 靠右上換行
												// 設定文字位置
												canvas.setTextMatrix(x + ((contentArray.length - 1) * kaiuFontSize),
														canvas.getPdfDocument().getPageSize().getHeight()
																- (y + (j * kaiuFontSize)));
												canvas.setRGBColorFill(0, 0, 80); // 顏色更改
												canvas.showText(contentArray[j]); // set text --from json
												canvas.endText();
												continue;
											}
											canvas.setTextMatrix(x, canvas.getPdfDocument().getPageSize().getHeight()
													- (y + (j * kaiuFontSize))); // 設定文字位置
											canvas.showText(contentArray[j]); // set text --from json
											canvas.endText();
										}
										continue;
									}
									canvas.setTextMatrix(x, canvas.getPdfDocument().getPageSize().getHeight() - y); // 設定文字位置
																													// (0,0)為左下角開始
									canvas.showText(content); // set text --from json
								}
								canvas.endText();

//								// Amy Hung: 想辨法讓它換行
//								Font font = new Font(normalFont);
//								ColumnText ct2 = new ColumnText(canvas);
//								Chunk c1 = new Chunk("（被保險人）姓名：金大寶 國家：中華民國 \n出生日期：99.5 身分證字號", font);
//								Phrase p1 = new Phrase(c1);
//								ct2.setSimpleColumn(p1, 134, 300, 618, 500, 15,
//										com.itextpdf.text.Element.ALIGN_JUSTIFIED);
//
//								ct2.go();

							}
							try {
								stamper.close();
								reader.close();
								pathList.add(pathMap.get("destination_PDF_path")); // 加入完成的PDF檔路徑到pathList
								// if (signatureData.getDataMap().get("docName") != null) { //判斷是否有簽名檔
								// 是則加到簽名檔list
								// //signatureDataList.add(signatureData);
								// }
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("PDF錯誤 錯誤檔案:" + pathMap.get("original_PDF_path"));
							}
							// pageNum++;
							pathMap = createPathMap(jmap, p, newPolicyNo, g, subPath);
							reader = new PdfReader(pathMap.get("original_PDF_path")); // input PDF path
						}
					}
				}
			}
			return pathList; // 回傳填寫完成的PDF pathList
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* 生成pdf所需路徑Map */
	private HashMap createPathMap(JsonNode jmap, int p, String newPolicyNo, int g, String subPath) {
		try {
			if (jmap.at("/page/" + g + "/" + p).asText().isEmpty()) {
				return null;
			}
			/* PCR-法人聲明除外 若核心接受此檔案即可刪除 */
			if (jmap.at("/page/" + g + "/" + p).asText().contains("UNBN05100")) {
				return null;
			}
			/*--------------------------------------*/
			HashMap<String, String> pathMap = new HashMap<>();
			NodeList settings = getNodeList(resourcePath + docVersion + "\\settings\\settings.xml");
			for (int i = 0; i < settings.getLength(); i++) {
				Node setting = settings.item(i);
				Element element = (Element) setting;
				if (element.getAttribute("id").equals(jmap.at("/page/" + g + "/" + p).asText())) {
					pathMap.put("original_PDF_path",
							resourcePath + docVersion + "\\documents\\" + element.getAttribute("pdf") + ".pdf"); // 原始PDF路徑
					pathMap.put("setting_xml_path",
							resourcePath + docVersion + "\\settings\\" + element.getAttribute("xml") + ".xml");// xml設定檔路徑
					if (subPath != null) {
						g = Integer.valueOf(subPath);
					}
					pathMap.put("destination_PDF_path", mainPath + newPolicyNo + "\\" + g + "\\" + this.pageNum + "\\"
							+ element.getAttribute("pdf").split("_")[0] + ".pdf");// 生成PDF路徑
					// pageNum++;
					break;
				}
				if (element.getAttribute("multiPage") != null && element.getAttribute("multiPage").equals("true")
						&& jmap.at("/page/" + g + "/" + p).asText().contains("UNBB02611")) { // 建議書
					String page = jmap.at("/page/" + g + "/" + p).asText().split("_")[2];
					pathMap.put("original_PDF_path", mainPath + newPolicyNo + "\\proposal\\" + page + ".pdf"); // 原始PDF路徑
					if (subPath != null) {
						g = Integer.valueOf(subPath);
					}
					pathMap.put("destination_PDF_path", mainPath + newPolicyNo + "\\" + g + "\\" + this.pageNum + "\\"
							+ jmap.at("/page/" + g + "/" + p).asText() + ".pdf");// 生成PDF路徑
					break;
				}
			}
			return pathMap;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Mobile Insured Error: Fail when generating pathMap.");
			System.out.println("Error page :" + jmap.at("/page/" + g + "/" + p).asText());
			return null;
		}
	}

	/* 取得XML NodeLsit */
	private static NodeList getNodeList(String settingXmlPath) {
		try {
			File xml = new File(settingXmlPath.replace("", "")); // xml path
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xml);
			doc.getDocumentElement().normalize();
			return doc.getElementsByTagName("item"); // 取得所有item標籤名 回傳Nodelist
		} catch (Exception e) {
			return null;
		}
	}

	/* 新增\n到字串該換行的位置 */
	private static String contentWrap(String content, int wrapIndex) {
		int contentLength = content.length();
		String[] contentArr = content.split("");
		String newContent = "";
		for (int i = 0; i < contentLength; i++) {
			if (i % wrapIndex == 0 && i != 0) {
				newContent += "\n";
			}
			newContent += contentArr[i];
		}
		return newContent;
	}

	/* 取得文字開始的X座標 */
	private static float getX(JsonNode jmap, Element itemElement, BaseFont bf, PdfContentByte canvas, String s) {
		String fieldContent = jmap
				.at("/" + itemElement.getAttribute("menuId") + "/" + s + itemElement.getAttribute("itemId")).asText();
		float kaiuFontSize = 12;
		float width = bf.getWidthPoint(fieldContent, kaiuFontSize);
		float start = (Float.parseFloat(itemElement.getAttribute("x")) / 100)
				* canvas.getPdfDocument().getPageSize().getWidth();
		float end = (Float.parseFloat(itemElement.getAttribute("w")) / 100)
				* canvas.getPdfDocument().getPageSize().getWidth();
		float offset = width / 2;
		if (itemElement.getAttribute("horAlign").equals("left")) { // 靠左
			return start;
		} else if (itemElement.getAttribute("horAlign").equals("right")) { // 靠右
			return start + (end - start - width);
		} else {
			return (start + end) / 2 - offset;
		}

	}

	/* 取得文字開始的Y座標 */
	private static float getY(JsonNode jmap, Element itemElement, BaseFont bf, PdfContentByte canvas, String s) {
		String fieldContent = jmap
				.at("/" + itemElement.getAttribute("menuId") + "/" + s + itemElement.getAttribute("itemId")).asText();

		float height = (bf.getAscent(fieldContent) - bf.getDescent(fieldContent)) / 100;
		float start = (Float.parseFloat(itemElement.getAttribute("y")) / 100)
				* canvas.getPdfDocument().getPageSize().getHeight();
		float end = (Float.parseFloat(itemElement.getAttribute("h")) / 100)
				* canvas.getPdfDocument().getPageSize().getHeight();
		float offset = height / 2;
		if (itemElement.getAttribute("verAlign").equals("top")) { // 靠上
			return start + offset;
		} else if (itemElement.getAttribute("verAlign").equals("bottom")) { // 靠下
			return start + (end - start + offset);
		} else {
			return (start + end) / 2 + offset;
		}
	}

	public static void setDocVersion(String Version) {
		docVersion = Version;
	}

	public static void testPdf() throws Exception {

		/* 字體設定 */
		/* 一般字型 */
		BaseFont normalFont = BaseFont.createFont(normalFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

		PdfReader reader = new PdfReader(resourcePath + "\\documents\\FATCA1.pdf"); // input PDF path

		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(mainPath + "test.pdf"));

		PdfContentByte canvas = stamper.getOverContent(1);

		canvas.setFontAndSize(normalFont, kaiuFontSize);

		Font font = new Font(normalFont);
		Font bold = new Font(normalFont, 12, Font.BOLD);

		ColumnText ct = new ColumnText(canvas);
		Rectangle rectangle = new Rectangle(53, 39, 709, 183);
		rectangle.setBorder(Rectangle.BOX);
		rectangle.setBorderWidth(2);

		canvas.rectangle(rectangle);

		ct.setSimpleColumn(rectangle);
		// ct.addElement(new Paragraph("AABB身故保險金受益人：（被保險人）\\n姓名：金大寶 國家：中華民國 出生日期：99.5
		// 身分證字號：F123123123 與被保險人關係：子女 順位：3 比例：34%", font));
		// int status = ct.go();

		Chunk c1 = new Chunk("身故保險金受益人：\n", bold);
		Chunk c2 = new Chunk("（被保險人）姓名：金大寶 \n國家：中華民國 \n出生日期：99.5 身分證字號", font);
		Phrase p1 = new Phrase(c1);
		p1.add(c2);
		Paragraph pg = new Paragraph(p1);
		ct.addElement(pg);
		ct.go();

		ColumnText ct2 = new ColumnText(canvas);

		ct2.setSimpleColumn(new Phrase(c2), 134, 300, 618, 500, 15, com.itextpdf.text.Element.ALIGN_JUSTIFIED);

		ct2.go();

		stamper.close();
		reader.close();

	}

	public static void main(String[] args) throws Exception {

//		boolean test = false;
//
//		if (test) {
//			testPdf();
//			return;
//		}
//
//		DocGenerator docGenerator = new DocGenerator("", "", "");
//		docGenerator.setDocVersion("");
//
//		try {
//
//			File jsonFile = new File(jsonPath);
//			String jsonData = new String(Files.readAllBytes(jsonFile.toPath()));
//			String policyNo = "AAA";
//
//			String jsonQuery = jsonData;
//			ObjectMapper mapper = new ObjectMapper();
//			JsonNode jMap;
//
//			jMap = mapper.readTree(jsonQuery); // 將json各值放入map
//
//			boolean pdfFillSuccess = docGenerator.startGenerate(jMap.at("/fillData").toString(), policyNo);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		// try {
		//
		// DocGenerator doc = new DocGenerator();
		// doc.setDocVersion("");
		// File jsonFile = new File("C:\\Users\\Weu\\Desktop\\test.json");
		// // File f = new File("C:"+mainPath+"AES128.txt");
		// String jsonData = new String(Files.readAllBytes(jsonFile.toPath()));
		// String newPolicyNo = "AAA";
		// doc.startGenerate(jsonData, newPolicyNo);
		//
		// // doc.startGenerate(jsonData,newPolicyNo,"1");
		// // if (Files.notExists(Paths.get(mainPath + newPolicyNo))) {
		// // Files.createDirectories(Paths.get(mainPath + newPolicyNo));
		// // }
		// // doc.splitProposal(jsonData,newPolicyNo);
		// // java.util.List<String> filePath = doc.createPdf(jsonData,
		// newPolicyNo,null);
		// // //產生PDF
		// // doc.createQuestionaire(jsonData,newPolicyNo,null);
		// // doc.createSignaturePdf(filePath);
		// // convertPdf2Tiff(filePath);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}

}
