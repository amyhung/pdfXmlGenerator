
package mpos.pdfXmlGenerator;

//import cn.com.n22.global.common.utils.PropertyUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import com.itextpdf.text.pdf.util.SmartPdfSplitter;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import sun.security.acl.WorldGroupImpl;

//

/**
 * Created by Kal on 2017/3/2.
 */
public class DocGenerator {

	public DocGenerator(String fontsPath, String dataJsonPath, String webPackagePath, String pdfSavePath) {
		this.mainPath = pdfSavePath + "\\";
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
	//static float kaiuFontSize = 10;

	// 特殊符號字型位置+大小
	static String symbolFontPath = "C:\\Users\\Weu\\Desktop\\DocGenerate\\pdfTest\\DejaVuSans.ttf";
	//static float dejaVuFontSize = 15;
	
	String temp="";

	// 問卷Map
	// private Map<String, List<JsonNode>> questMap = new LinkedHashMap<>();

	public boolean startGenerate(String fillData, String newPolicyNo) throws Exception {
		return startGenerate(fillData, newPolicyNo, null);
	}

	 public boolean startGenerate(String fillData, String newPolicyNo,String subPath) {
	        try {

	            //檔案路徑
	            if (Files.notExists(Paths.get(mainPath + newPolicyNo))) {
	                Files.createDirectories(Paths.get(mainPath + newPolicyNo));
	            }
	            else if(!Files.notExists(Paths.get(mainPath + newPolicyNo))&&subPath!=null){  //刪除特定主約 重新套印
	                FileUtils.deleteDirectory(new File(mainPath + newPolicyNo+"/"+subPath));
	            }
	            else if(!Files.notExists(Paths.get(mainPath + newPolicyNo))&&subPath==null){ //刪除全部主約 重新套印
	                for(int i = 0 ; i<3;i++) {
	                    try {
	                        FileUtils.deleteDirectory(new File(mainPath + newPolicyNo + "/" + i));
	                    }catch (Exception e){
	                        System.out.println("刪除:"+mainPath + newPolicyNo + "/" + i+"時發生錯誤");
	                    }
	                }
	            }
	            else {
	                System.out.println("Mobile Insured Error: File directory already exist.");
	                return false;
	            }

	            //boolean containProposal = splitProposal(fillData,newPolicyNo);
	            boolean containProposal = false;
	            java.util.List<String> filePath = createPdf(fillData, newPolicyNo,subPath);        //產生PDF
	            //createQuestionaire(fillData,newPolicyNo,subPath);
	            //createSignaturePdf(filePath);
	            if(containProposal){  //刪除建議書
	                FileUtils.deleteDirectory(new File(mainPath + newPolicyNo + "/proposal"));
	            }
	            if (filePath!=null&&uiJsonPageNum <= filePath.size()) {
	                return true;
	            } else {
	                //缺頁時刪除要保文件
	                FileUtils.deleteDirectory(new File(mainPath + newPolicyNo));
	                System.out.println("Mobile Insured Error: Fail when generating pdf or tif.");
	                //需要頁數,套印頁數
	                System.out.println("Needed page :"+uiJsonPageNum+",generated page:"+filePath==null?0:filePath.size());
	                return false;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	private static float adjustFontSizeToWidth(BaseFont font, float defaultFontSize, String text, float fitWidth) {
		if (font == null || defaultFontSize <= 0) {
			return defaultFontSize;
		}

		float fontSize = defaultFontSize;
		while (font.getWidthPoint(text, fontSize) > fitWidth) {
			fontSize -= 0.2;

			if (fontSize <= 1)
				break;
		}

		return fontSize < 1 ? 1 : fontSize;
	}
	
    /*根據jsonData+xml設定檔產生PDF並回傳*/
    private java.util.List<String> createPdf(String jsonData, String newPolicyNo,String subPath) {

		float kaiuFontSize = 10;
		float dejaVuFontSize = 15;
		
        try {
            /*字體設定*/
            /*一般字型*/
            BaseFont normalFont = BaseFont.createFont(normalFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            /*符號字型 */
            BaseFont symbolFont = BaseFont.createFont(symbolFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            /*json2map*/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jmap = mapper.readTree(jsonData); //將json各值放入map
            /*取得文件分組數   (主約數)*/
            int groupNum = jmap.at("/page").size();
            
			// 組成真正的保單號碼:依有多少主約組成保單號碼，傳入的 newPolicyno 其實只是取號碼而已
			// 例如 newPolicyno = 7000000001 時
			// 單主約保單號碼為 70000000010
			// 多主約(3個主約)保單碼碼為
			// 70000000011
			// 70000000012
			// 70000000013
			String[] policyCodes = new String[] { "", "", "" }; // 各主約的保單號碼
            String[][] splitPolicyCodes = new String[3][11]; // 各主約保單號碼 - 拆字字母
            
			if (groupNum == 1) {
				policyCodes[0] = newPolicyNo + "0";
				splitPolicyCodes[0] = policyCodes[0].split("");
			} else {
				for (int p = 1; p <= groupNum; p++) {
					policyCodes[p - 1] = newPolicyNo + p;
					splitPolicyCodes[p - 1] = policyCodes[p - 1].split("");
				}
			}
			// end
			
            java.util.List<String> pathList = new ArrayList<>();
            for (int g = 0; g < groupNum; g++){
            /*取得頁數*/
            int pSize = jmap.at("/page/"+g).size();
            uiJsonPageNum+=pSize;

            /*開始處理*/
            for (int p = 0; p < pSize; p++) {

                /*生成路徑Map + 產生路徑資料夾*/
                HashMap<String, String> pathMap = createPathMap(jmap, p, newPolicyNo,g,subPath);

                /*路徑Map為Null時處理下一頁*/
                if (pathMap == null||pathMap.size()==0) {
                    continue;
                }

                    String pageId = jmap.at("/page/" + g + "/" + p).asText();

 					// 為了付款授權書所做的特殊處理
                     String unbn600FirstPaymentChecked = "";
                     String unbn600RenewalPaymentChecked = "";
                     String unbnNum="";
                     
                     // 如果目前要產出的是付款授權書頁面
                     if ("UNBN00600_1_1".equals(pageId) || "UNBN00600_2_1".equals(pageId)
                             || "UNBN00600_3_1".equals(pageId) || "UNBN00600_4_1".equals(pageId)
                             || "UNBN00600_5_1".equals(pageId) || "UNBN00600_6_1".equals(pageId)) {
                         
                         if("UNBN00600_1_1".equals(pageId)) {
                             unbnNum = "num1";
                         } else if("UNBN00600_2_1".equals(pageId)) {
                             unbnNum = "num2";
                         } else if("UNBN00600_3_1".equals(pageId)) {
                             unbnNum = "num3";
                         } else if("UNBN00600_4_1".equals(pageId)) {
                             unbnNum = "num4";
                         }else if("UNBN00600_5_1".equals(pageId)) {
                             unbnNum = "num5";
                         } else if("UNBN00600_6_1".equals(pageId)) {
                             unbnNum = "num6";
                         }
 
                         // 以下兩個參數是前端給的，告訴後端此主約的信用卡套用至首期/續期
                         // 會這樣做是因為目前此要保文件的設計維度有問題，因此只好在後端多判斷它
                         String covNum = "cov" + (g + 1);
                         
                         if (!jmap.at("/payment/" + unbnNum + "_" + covNum + "_first").asText().isEmpty()) {
                             unbn600FirstPaymentChecked = "v";
                         }
                         
                         if (!jmap.at("/payment/" + unbnNum + "_" + covNum + "_renewal").asText().isEmpty()) {
                             unbn600RenewalPaymentChecked = "v";
                         }
 
                     }
                     // end

                /*PDF設定*/
                PdfReader reader = new PdfReader(pathMap.get("original_PDF_path")); //input PDF path
                 /*設定檔內容為空或找不到設定檔 直接存PDF 處理下一頁*/

                for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++) {
                    //SignatureData signatureData = new SignatureData();
                     /*取得xml items*/
                    NodeList items = getNodeList(pathMap.get("setting_xml_path"));
                    if (Files.notExists(Paths.get(mainPath + newPolicyNo + "/" + (subPath==null?g:Integer.valueOf(subPath)) + "/" + this.pageNum))) {
                        Files.createDirectories(Paths.get(mainPath + newPolicyNo + "/" +  (subPath==null?g:Integer.valueOf(subPath)) + "/" + this.pageNum));
                    }
                    this.pageNum++;
                    if (items == null) { //若不須套印 不做下面動作
						PdfStamper stamper = new PdfStamper(reader,
								new FileOutputStream(pathMap.get("destination_PDF_path"))); // output PDF
						// path
						stamper.close();
						reader.close();
                        pathList.add(pathMap.get("destination_PDF_path"));
//                        pageNum++;
                        createPathMap(jmap, p, newPolicyNo, g,subPath);
                        continue;
                    }
                    Set<String> set = new HashSet<String>();
                    Element ele = ((Element) items.item(5));
                    if (ele!=null&&ele.getAttribute("menuId").equals("questionnaire")) {
//                        String barcode = jmap.at("/page/"+g+ "/" + p).asText();
//                        List<JsonNode> questList=findQuestWithBarCode(barcode,jmap);
//                        questMap.put(jmap.at("/page/"+g+ "/" + p).asText(),questList);
                        this.pageNum--;
                        continue;
                    } else {
                        set.add("");
                    }
                    
                    for (String s : set) {     //s = 問卷json中間層字串

//                        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(
//                                ESAPI.encoder().canonicalize(pathMap.get("destination_PDF_path")))); // output PDF  path
                    	PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(pathMap.get("destination_PDF_path")));                    	
                        
                        for (int i = 0; i < items.getLength(); i++) {
                        	
                            Node item = items.item(i);
                            Element itemElement = (Element) item;

                            PdfContentByte canvas = stamper.getOverContent(pageNumber);   //第pageNumber頁內容  目前都為一份pdf一頁
                            canvas.beginText();
                            float x = 0;
                            float y = 0;
                            
							// added 12/23 
							float itemFontSize = kaiuFontSize;
							
                            // 有自訂 font size, 以自訂的 font size 為主					
							if (itemElement.hasAttribute("fontSize")) {
								try {
									itemFontSize = Float.valueOf(itemElement.getAttribute("fontSize"));
								} catch (Exception e) {
								}
							} // end
							
                            if (itemElement.getAttribute("x") != null && itemElement.getAttribute("y") != null &&
                                !itemElement.getAttribute("x").equals("") &&
                                !itemElement.getAttribute("y").equals("")) {

                                if (itemElement.getAttribute("signatureName") != null &&
                                    !itemElement.getAttribute("signatureName").equals("")) {
//                                    String fieldContent = jmap
//                                            .at("/signature/" + s + itemElement.getAttribute("itemId"))
//                                            .asText();
//                                    /*BASE64編碼*/
//                                    com.itextpdf.text.Image sigImage = stringDecodeToImage(fieldContent);
//                                    /*此段加入簽名檔data 之後產生簽名檔*/
//                                    if (signatureData.getDataMap().get("docName") == null) {
//                                        File xml = new File(ESAPI.encoder().canonicalize(pathMap.get("setting_xml_path")));  //xml path
//                                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//                                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//                                        Document doc = dBuilder.parse(xml);
//                                        doc.getDocumentElement().normalize();
//                                        NodeList titleItems = doc.getElementsByTagName("title");
//                                        Node title = titleItems.item(0);
//                                        Element titleElement = (Element) title;
//                                        if(titleElement.getAttribute("name").contains("財務狀況告知書")){
//                                            signatureData.getDataMap().put("docName", "財務狀況告知書");
//                                        }else{
//                                            signatureData.getDataMap().put("docName", titleElement.getAttribute("name"));
//                                        }
//                                        signatureData.getDataMap().put("path", pathMap.get("destination_PDF_path"));
//                                        signatureData.getDataMap().put("numbering", newPolicyNo);
//                                    }
//                                    String key = itemElement.getAttribute("signatureId");
//                                    String name = "";
//                                    if(!jmap.at("/signature/" + key).asText().isEmpty()) {
//                                        name = "(" + jmap.at("/signature/" + key).asText() + ")";
//                                    }
//                                    signatureData.getDataMap().put(key + "Name", name); //加入姓名到簽名檔nameMap
//                                    signatureData.getSigMap().put(key, fieldContent); //加入圖檔到簽名檔sigMap
//                                    /*簽名圖檔Decode失敗 直接回傳null*/
//                                    if (sigImage == null) {
////                                        stamper.close();
////                                        return null;
//                                        canvas.endText();
//                                        continue;
//                                    }
//                                    setImgSizeAndPos(sigImage, itemElement, canvas);     //設定圖片位置及大小
//                                    canvas.setTextMatrix(0, 0);
//                                    canvas.setFontAndSize(normalFont, kaiuFontSize);
//                                    try {
//                                        canvas.addImage(sigImage);
//                                    }catch (Exception e){
//                                        System.out.println("要保書套印簽名發生錯誤 檔案:"+pathMap.get("destination_PDF_path"));
//                                        System.out.println(e.getMessage());
//                                    }

                                } else if (jmap.at("/" + itemElement.getAttribute("menuId") + "/" + s +                                   
                                    itemElement.getAttribute("itemId")).asText().equals("v")
                                		|| "creditCard_first".equals(itemElement.getAttribute("itemId"))
                                		|| "creditCard_renewal".equals(itemElement.getAttribute("itemId"))) {
                                	 // 打勾字型及位置設定
                                    canvas.setFontAndSize(symbolFont, dejaVuFontSize); // set font and size
                                    x = getX(jmap, itemElement, symbolFont, canvas, s, kaiuFontSize);
                                    y = getY(jmap, itemElement, symbolFont, canvas, s);
                                } else { // 其他字型及位置設定
                                    canvas.setFontAndSize(normalFont, itemFontSize); // set font and size
                                    x = getX(jmap, itemElement, normalFont, canvas, s, itemFontSize);
                                    y = getY(jmap, itemElement, normalFont, canvas, s);
                                }

                                String content ="";
								
								if (itemElement.getAttribute("menuId").equals("PARAMS")
										&& itemElement.getAttribute("itemId").equals("POLICY_CODE_1")) {
									content = policyCodes[0];
								} else if (itemElement.getAttribute("menuId").equals("PARAMS")
											&& itemElement.getAttribute("itemId").equals("POLICY_CODE_2")) {
									content = policyCodes[1];
								} else if (itemElement.getAttribute("menuId").equals("PARAMS")
											&& itemElement.getAttribute("itemId").equals("POLICY_CODE_3")) {
                                    content = policyCodes[2];
								} else if (itemElement.getAttribute("menuId").equals("PARAMS")
                                    		&& itemElement.getAttribute("itemId").equals("POLICY_CODE")) {
									// 依其在哪個主約，套印上該主約的保單號碼
                                    content = policyCodes[g];
                                } else if(itemElement.getAttribute("menuId").equals("PARAMS")
                                    && itemElement.getAttribute("itemId").equals("SPLIT_POLICY_CODE")
                                    && itemElement.hasAttribute("policyCodeNum")) {
                                
                                    Integer pos = null;
                                    
                                    try {
                                        pos = Integer.parseInt(itemElement.getAttribute("policyCodeNum"));
                                    } catch (Exception e) {}
                                    
                                    if (pos != null && splitPolicyCodes[g] != null
                                            && splitPolicyCodes[g][0] != null
                                            && splitPolicyCodes[g].length > pos - 1) {
                                        content = splitPolicyCodes[g][pos - 1];
                                    }
                                                                                             
                                } else if (itemElement.getAttribute("menuId").equals("PARAMS")
                                    && itemElement.hasAttribute("policyCodeNum")) {

                                    Integer pos = null;
                                    int policyCodeIndex = 0;

                                    if ("SPLIT_POLICY_CODE_1".equals(itemElement.getAttribute("itemId"))) {
                                        policyCodeIndex = 0;
                                    } else if ("SPLIT_POLICY_CODE_2".equals(itemElement.getAttribute("itemId"))) {
                                        policyCodeIndex = 1;
                                    } else if ("SPLIT_POLICY_CODE_3".equals(itemElement.getAttribute("itemId"))) {
                                        policyCodeIndex = 2;
                                    }

                                    try {
                                        pos = Integer.parseInt(itemElement.getAttribute("policyCodeNum"));
                                    } catch (Exception e) {}
                                    
                                    if (pos != null && splitPolicyCodes[policyCodeIndex] != null
                                            && splitPolicyCodes[policyCodeIndex].length > pos - 1) {
                                        content = splitPolicyCodes[policyCodeIndex][pos - 1];
                                    }
                                    
                                    if(content == null) { // error
                                    	content = "";
                                    }
                                    
								} else {
									content = jmap.at("/" + itemElement.getAttribute("menuId") + "/" + s
											+ itemElement.getAttribute("itemId")).asText(); // json value 取得
                                }
                                
                                if (itemElement.hasAttribute("printMode")
												&& "Chunk".equals(itemElement.getAttribute("printMode"))) {

                                    // TODO：Amy 這裡寫的很不好，有時間再改吧
                                    int wordLength = content.length();
                                                                                
                                    String greaterLength = itemElement.getAttribute("greaterLength");
                                    String fontSize = itemElement.getAttribute("fontSize");
                                    String greaterLengthFontSize = itemElement.getAttribute("greaterLengthFontSize");
                                    String leading = itemElement.getAttribute("leading");
                                    String greaterLeading = itemElement.getAttribute("greaterLeading");

                                    String iix = itemElement.getAttribute("iix");
                                    String iiy = itemElement.getAttribute("iiy");
                                    String urx = itemElement.getAttribute("urx");
                                    String ury = itemElement.getAttribute("ury");

                                    float fIIx = 0;
                                    float fIIy = 0;
                                    float fURx = 0;
                                    float fURy = 0;
                                    int iGreaterLength = 0;
                                    float fFontSize = 0;
                                    float fGreaterLengthFontSize = 0;
                                    float fLeading = 0;
                                    float fGreaterLeading = 0;
                                    float fAutoAdjustHightWhenNotGreater = 0;

                                    try {
                                        
                                        fIIx = Float.valueOf(iix);
                                        fIIy = Float.valueOf(iiy);
                                        fURx = Float.valueOf(urx);
                                        fURy = Float.valueOf(ury);												
                                        
                                        iGreaterLength = Integer.parseInt(greaterLength);
                                        fFontSize = Float.valueOf(fontSize);												
                                        fLeading = Float.valueOf(leading);
                                        fGreaterLengthFontSize = Float.valueOf(greaterLengthFontSize);
                                        fGreaterLeading = Float.valueOf(greaterLeading);

                                        // 如果字數沒有超過指定的字數，要調整的 x 值
                                        // 因為如果字數沒有超過的情況之下，它會靠上，加上調整值讓版面不那麼奇怪
										if(itemElement.hasAttribute("autoAdjustHightWhenNotGreater")) {
											try {
												fAutoAdjustHightWhenNotGreater = Float.valueOf(itemElement.getAttribute("autoAdjustHightWhenNotGreater"));
											} catch (Exception e) {
											}											
										}
										
                                        // 長度大於指定的長度時
										if(wordLength > iGreaterLength) {
											fFontSize = fGreaterLengthFontSize;
											fLeading = fGreaterLeading;
										} else {
											fIIy = fIIy + fAutoAdjustHightWhenNotGreater;
										}								
                                        
                                    } catch (Exception e) {
                                    	canvas.endText(); // must do this, 不下這行會錯, 可能是因為 beginText 了卻沒有寫東西
                                        continue; // 發生錯誤，此欄位資料跳過不套印
                                    }

                                    Font font = new Font(normalFont);
                                    ColumnText ct2 = new ColumnText(canvas);
                                    font.setSize(fFontSize);
                                    Chunk c1 = new Chunk(content, font);
                                    Phrase p1 = new Phrase(c1);
                                    
                                    int align = com.itextpdf.text.Element.ALIGN_JUSTIFIED;
                                    
									// 如果字數沒有大於設定的，要不要置中
									if ((wordLength < iGreaterLength) && itemElement.hasAttribute("autoAlignCenterWhenNotGreater")) {
										align = com.itextpdf.text.Element.ALIGN_CENTER;
									}
									
									ct2.setSimpleColumn(p1, fIIx, fIIy, fURx, fURy, fLeading, align);								

                                    ct2.go();

                                } else {

									// TODO: add 12/23 有自訂超過字數要設定字型大小, 兩個一定都要設
									if (itemElement.hasAttribute("greaterLength")
											&& itemElement.hasAttribute("greaterLengthFontSize")) {
										try {
											int greaterWordLength = Integer.parseInt(itemElement.getAttribute("greaterLength"));
											float greaterLengthFontSize = Float.valueOf(itemElement.getAttribute("greaterLengthFontSize"));
											if (content != null && content.length() > greaterWordLength) {
												itemFontSize = greaterLengthFontSize;
												canvas.setFontAndSize(normalFont, itemFontSize);
												x = getX(jmap, itemElement, normalFont, canvas, s, itemFontSize);
												y = getY(jmap, itemElement, normalFont, canvas, s);
											}
										} catch (Exception e) {
											// do nothing, 還是以原來的 font size 為主
										}
									}									
									
                                    canvas.setRGBColorFill(0, 0, 80); // 顏色更改
                                    
                                    if (itemElement.getAttribute("wrapLine") != null &&
                                        !itemElement.getAttribute("wrapLine").equals("")) {   //換行
                                        int offset = 1;
                                        if (content.matches("^\\w+$")) {   //若字串皆為英文或數字  換行字數*2
                                            offset = 2;
                                        }
                                        content = contentWrap(content,
                                                              Integer.parseInt(itemElement.getAttribute("wrapLine")) *
                                                              offset);
                                        String[] contentArray = content.split("\n");
                                        canvas.endText();
                                        for (int j = 0; j < contentArray.length; j++) {

                                            if(j == 0) {
                                                x = getXForWrapLine(jmap, itemElement, normalFont, canvas, contentArray[j], itemFontSize);
                                                y = getYForWrapLine(jmap, itemElement, normalFont, canvas, contentArray[j], contentArray.length);
                                            }

                                            canvas.beginText();

                                            if (itemElement.getAttribute("horAlign").equals("right")) {   //靠右上換行
                                                //設定文字位置
                                                canvas.setTextMatrix(x + ((contentArray.length - 1) * itemFontSize),
                                                                     canvas.getPdfDocument().getPageSize().getHeight() -
                                                                     (y + (j * itemFontSize)));
                                                canvas.setRGBColorFill(0, 0, 80);       //顏色更改
                                                canvas.showText(contentArray[j]);  // set text   --from json
                                                canvas.endText();
                                                continue;
                                            }
                                            canvas.setTextMatrix(x, canvas.getPdfDocument().getPageSize().getHeight() -
                                                                    (y + (j * itemFontSize)));      //設定文字位置
                                            canvas.showText(contentArray[j]);  // set text   --from json
                                            canvas.endText();
                                        }
                                        continue;
                                    }
    
									    // 為了付款授權書所做的特殊處理
										if ("payment".equals(itemElement.getAttribute("menuId"))
												&& (unbnNum + "_creditCard_first").equals(itemElement.getAttribute("itemId"))) {
											content = unbn600FirstPaymentChecked;
										}

										if ("payment".equals(itemElement.getAttribute("menuId"))
												&& (unbnNum + "_creditCard_renewal").equals(itemElement.getAttribute("itemId"))) {
											content = unbn600RenewalPaymentChecked;
										}
										// end 
                                                                                
                                     //設定文字位置 (0,0)為左下角開始
                                    canvas.setTextMatrix(x, canvas.getPdfDocument().getPageSize().getHeight() - y);
                                    canvas.showText(content);  // set text   --from json, 給 null

                                }
                                                              
                            }

                            canvas.endText();

                        }

                        try {
                            stamper.close();
                            reader.close();
                            pathList.add(pathMap.get("destination_PDF_path"));  //加入完成的PDF檔路徑到pathList
//                            if (signatureData.getDataMap().get("docName") != null) {    //判斷是否有簽名檔 是則加到簽名檔list
//                                signatureDataList.add(signatureData);
//                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("PDF錯誤 錯誤檔案:"+pathMap.get("original_PDF_path"));
                        }
//                        pageNum++;
                        pathMap = createPathMap(jmap, p, newPolicyNo, g,subPath);
                        reader = new PdfReader(pathMap.get("original_PDF_path")); //input PDF path
                    }
                }
            }
        }
            return pathList;    //回傳填寫完成的PDF pathList
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }	
	    
	/* 根據jsonData+xml設定檔產生PDF並回傳 */
	private java.util.List<String> createPdf2(String jsonData, String newPolicyNo, String subPath) {

		float kaiuFontSize = 10;
		float dejaVuFontSize = 15;
		
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
			
			String[] policyCodes = new String[] { "", "", "" }; // 各主約的保單號碼
			String[][] splitPolicyCodes = new String[3][11]; // 各主約保單號碼 - 拆字字母

			if (groupNum == 1) {
				policyCodes[0] = newPolicyNo + "0";
				splitPolicyCodes[0] = policyCodes[0].split("");
			} else {
				for (int p = 1; p <= groupNum; p++) {
					policyCodes[p - 1] = newPolicyNo + p;
					splitPolicyCodes[p - 1] = policyCodes[p - 1].split("");
				}
			}

			java.util.List<String> pathList = new ArrayList<>();
			
			// 1. 依有多少主約，逐一產出各主約的 pdf 檔
			for (int g = 0; g < groupNum; g++) {
				
				/* 取得該主約有多少頁數（pageId)要套版 pdf */
				int pSize = jmap.at("/page/" + g).size();
				uiJsonPageNum += pSize;
				
				// 2.依序產出各 pageId(即 pdf)
				for (int p = 0; p < pSize; p++) {
					
					/* 生成路徑Map + 產生路徑資料夾 */
					HashMap<String, String> pathMap = createPathMap(jmap, p, newPolicyNo, g, subPath);					
					
					/* 路徑Map為Null時處理下一頁 */
					if (pathMap == null || pathMap.size() == 0) {
						continue;
					}
					
					String pageId = jmap.at("/page/" + g + "/" + p).asText();

					// 為了付款授權書所做的特殊處理
					String unbn600FirstPaymentChecked = "";
					String unbn600RenewalPaymentChecked = "";
					String unbnNum="";
					
					// 如果目前要產出的是付款授權書頁面
					if ("UNBN00600_1_1".equals(pageId) || "UNBN00600_2_1".equals(pageId)
							|| "UNBN00600_3_1".equals(pageId) || "UNBN00600_4_1".equals(pageId)
							|| "UNBN00600_5_1".equals(pageId) || "UNBN00600_6_1".equals(pageId)) {
						
						if("UNBN00600_1_1".equals(pageId)) {
							unbnNum = "num1";
						} else if("UNBN00600_2_1".equals(pageId)) {
							unbnNum = "num2";
						} else if("UNBN00600_3_1".equals(pageId)) {
							unbnNum = "num3";
						} else if("UNBN00600_4_1".equals(pageId)) {
							unbnNum = "num4";
						}else if("UNBN00600_5_1".equals(pageId)) {
							unbnNum = "num5";
						} else if("UNBN00600_6_1".equals(pageId)) {
							unbnNum = "num6";
						}

						// 以下兩個參數是前端給的，告訴後端此主約的信用卡套用至首期/續期
						// 會這樣做是因為目前此要保文件的設計維度有問題，因此只好在後端多判斷它
						String covNum = "cov" + (g + 1);
						
						if (!jmap.at("/payment/" + unbnNum + "_" + covNum + "_first").asText().isEmpty()) {
							unbn600FirstPaymentChecked = "v";
						}
						
						if (!jmap.at("/payment/" + unbnNum + "_" + covNum + "_renewal").asText().isEmpty()) {
							unbn600RenewalPaymentChecked = "v";
						}

					}
					// end					

					/* PDF設定 */
					PdfReader reader = new PdfReader(pathMap.get("original_PDF_path")); // input PDF path
					
					/* 設定檔內容為空或找不到設定檔 直接存PDF 處理下一頁 */	
					// 3.依每份 pdf 裡有多少 page, 目前行動投保一份 Pdf 只會有一頁
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
							PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(pathMap.get("destination_PDF_path")));
							AcroFields form = stamper.getAcroFields();
						
							for (int i = 0; i < items.getLength(); i++) {				
								
								Node item = items.item(i);
								Element itemElement = (Element) item;

								PdfContentByte canvas = stamper.getOverContent(pageNumber); // 第pageNumber頁內容, 目前都為一份pdf一頁
								canvas.beginText();
								float x = 0;
								float y = 0;
								
								// TODO: add 12/23
								float itemFontSize = kaiuFontSize;
								
								// 有自訂 font size, 以自訂的 font size 為主
								if (itemElement.hasAttribute("fontSize")) {
									try {
										itemFontSize = Float.valueOf(itemElement.getAttribute("fontSize"));
									} catch (Exception e) {
									}
								} // end
																
								if (itemElement.getAttribute("x") != null && itemElement.getAttribute("y") != null
										&& !itemElement.getAttribute("x").equals("")
										&& !itemElement.getAttribute("y").equals("")) {

									if (itemElement.getAttribute("signatureName") != null
											&& !itemElement.getAttribute("signatureName").equals("")) {
										
										canvas.setTextMatrix(0, 0);
										canvas.setFontAndSize(normalFont, kaiuFontSize);

									} else if (jmap
											.at("/" + itemElement.getAttribute("menuId") + "/" + s
													+ itemElement.getAttribute("itemId")) // 打勾字型及位置設定
											.asText().equals("✔")) {
										canvas.setFontAndSize(symbolFont, dejaVuFontSize); // set font and size
										x = getX(jmap, itemElement, symbolFont, canvas, s, kaiuFontSize);
										y = getY(jmap, itemElement, symbolFont, canvas, s);
									} else { // 其他字型及位置設定
										canvas.setFontAndSize(normalFont, itemFontSize); // set font and size // TODO: add 12/23
										x = getX(jmap, itemElement, normalFont, canvas, s, itemFontSize); // TODO: add 12/23
										y = getY(jmap, itemElement, normalFont, canvas, s);
									}

									String content ="";																		
									
									//BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, null, null, false);
							       								
									 BaseFont bf = BaseFont.createFont(normalFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, null, null, false);
									if(itemElement.hasAttribute("fieldName")) {
										
										content = jmap.at("/" + itemElement.getAttribute("menuId") + "/" + s + itemElement.getAttribute("itemId")).asText();
										//content = content.replaceAll("\n", "\r");
										
										// TODO: 此做法 pdf to tiff 時，中文會莫名的亂碼，尚無解，先不用它
										form.setField(itemElement.getAttribute("fieldName"), content);
										form.setFieldProperty(itemElement.getAttribute("fieldName"), "textfont", bf, null);	
										
									} else {
										
										if (itemElement.getAttribute("menuId").equals("PARAMS")
												&& itemElement.getAttribute("itemId").equals("POLICY_CODE_1")) {
											content = policyCodes[0];
										} else if (itemElement.getAttribute("menuId").equals("PARAMS")
													&& itemElement.getAttribute("itemId").equals("POLICY_CODE_2")) {
											content = policyCodes[1];
										} else if (itemElement.getAttribute("menuId").equals("PARAMS")
													&& itemElement.getAttribute("itemId").equals("POLICY_CODE_3")) {
											content = policyCodes[2];
										} else if(itemElement.getAttribute("menuId").equals("PARAMS")
													&& itemElement.getAttribute("itemId").equals("POLICY_CODE")) {
											content = policyCodes[g];
										} else if(itemElement.getAttribute("menuId").equals("PARAMS")
												&& itemElement.getAttribute("itemId").equals("SPLIT_POLICY_CODE")
												&& itemElement.hasAttribute("policyCodeNum")) {
											
												Integer pos = null;
												
												try {
													pos = Integer.parseInt(itemElement.getAttribute("policyCodeNum"));
												} catch (Exception e) {}
												
												if (pos != null && splitPolicyCodes[g] != null
														&& splitPolicyCodes[g][0] != null
														&& splitPolicyCodes[g].length > pos - 1) {
													content = splitPolicyCodes[g][pos - 1];
												}
												
										} else if (itemElement.getAttribute("menuId").equals("PARAMS")
												&& itemElement.hasAttribute("policyCodeNum")) {

											Integer pos = null;
											int policyCodeIndex = 0;

											if ("SPLIT_POLICY_CODE_1".equals(itemElement.getAttribute("itemId"))) {
												policyCodeIndex = 0;
											} else if ("SPLIT_POLICY_CODE_2".equals(itemElement.getAttribute("itemId"))) {
												policyCodeIndex = 1;
											} else if ("SPLIT_POLICY_CODE_3".equals(itemElement.getAttribute("itemId"))) {
												policyCodeIndex = 2;
											}

											try {
												pos = Integer.parseInt(itemElement.getAttribute("policyCodeNum"));
											} catch (Exception e) {}
											
											if (pos != null && splitPolicyCodes[policyCodeIndex] != null
													&& splitPolicyCodes[policyCodeIndex][0] != null
													&& splitPolicyCodes[policyCodeIndex].length > pos - 1) {
												content = splitPolicyCodes[policyCodeIndex][pos - 1];
											}
											
											if(content == null)
												content = "";

										} else {
											content = jmap.at("/" + itemElement.getAttribute("menuId") + "/" + s
													+ itemElement.getAttribute("itemId")).asText(); // json value 取得
										}
										
										if (itemElement.hasAttribute("printMode")
												&& "Chunk".equals(itemElement.getAttribute("printMode"))) {
											
											// TODO：Amy 這裡寫的很不好，有時間再改吧
											int wordLength = content.length();
											
											String greaterLength = itemElement.getAttribute("greaterLength");
											String fontSize = itemElement.getAttribute("fontSize");
											String greaterLengthFontSize = itemElement.getAttribute("greaterLengthFontSize");
											String leading = itemElement.getAttribute("leading");
											String greaterLeading = itemElement.getAttribute("greaterLeading");
																						
											String iix = itemElement.getAttribute("iix");
											String iiy = itemElement.getAttribute("iiy");
											String urx = itemElement.getAttribute("urx");
											String ury = itemElement.getAttribute("ury");
											
											float fIIx = 0;
											float fIIy = 0;
											float fURx = 0;
											float fURy = 0;
											int iGreaterLength = 0;
											float fFontSize = 0;
											float fGreaterLengthFontSize = 0;
											float fLeading = 0;
											float fGreaterLeading = 0;
											float fAutoAdjustHightWhenNotGreater=0;
											
											try {
												
												fIIx = Float.valueOf(iix);
												fIIy = Float.valueOf(iiy);
												fURx = Float.valueOf(urx);
												fURy = Float.valueOf(ury);												
												
												iGreaterLength = Integer.parseInt(greaterLength);
												fFontSize = Float.valueOf(fontSize);												
												fLeading = Float.valueOf(leading);
												fGreaterLengthFontSize = Float.valueOf(greaterLengthFontSize);
												fGreaterLeading = Float.valueOf(greaterLeading);
												
												if(itemElement.hasAttribute("autoAdjustHightWhenNotGreater")) {
													fAutoAdjustHightWhenNotGreater = Float.valueOf(itemElement.getAttribute("autoAdjustHightWhenNotGreater"));
												}																								
												
												// 長度大於指定的長度時
												if(wordLength > iGreaterLength) {
													fFontSize = fGreaterLengthFontSize;
													fLeading = fGreaterLeading;
												} else {
													fIIy = fIIy + fAutoAdjustHightWhenNotGreater;
												}
												
											} catch (Exception e) {
												canvas.endText();
												continue; // 發生錯誤，此欄位資料跳過不套印
											}

											Font font = new Font(normalFont);
											ColumnText ct2 = new ColumnText(canvas);
											font.setSize(fFontSize);
											Chunk c1 = new Chunk(content, font);

											Phrase p1 = new Phrase(c1);
											
											Rectangle rect = new Rectangle(fIIx, fIIy, fURx, fURy);
											
											int align = com.itextpdf.text.Element.ALIGN_JUSTIFIED;
											
											// 如果字數沒有大於設定的，要不要置中
											if ((wordLength < iGreaterLength) && itemElement.hasAttribute("autoAlignCenterWhenNotGreater")) {
												align = com.itextpdf.text.Element.ALIGN_CENTER;
											}
											
											ct2.setSimpleColumn(p1, fIIx, fIIy, fURx, fURy, fLeading, align);
															
											ct2.go();

										} else {																						

											// TODO: add 12/23 有自訂超過字數要設定字型大小, 兩個一定都要設
											if (itemElement.hasAttribute("greaterLength")
													&& itemElement.hasAttribute("greaterLengthFontSize")) {
												try {
													int greaterWordLength = Integer.parseInt(itemElement.getAttribute("greaterLength"));
													float greaterLengthFontSize = Float.valueOf(itemElement.getAttribute("greaterLengthFontSize"));
													if (content != null && content.length() > greaterWordLength) {
														itemFontSize = greaterLengthFontSize;
														canvas.setFontAndSize(normalFont, itemFontSize);
														x = getX(jmap, itemElement, normalFont, canvas, s, itemFontSize);
														y = getY(jmap, itemElement, normalFont, canvas, s);
													}
												} catch (Exception e) {
													// do nothing, 還是以原來的 font size 為主
												}
											}
											
											int wrapline = 0;
											if(itemElement.hasAttribute("wrapLine")) {
												try {
													wrapline = Integer.parseInt(itemElement.getAttribute("wrapLine"));
												} catch (Exception e) {
													// do nothing
												}												
											} // end
											
											canvas.setRGBColorFill(50, 0, 80); // 顏色更改
											
											if (itemElement.getAttribute("wrapLine") != null
													&& !itemElement.getAttribute("wrapLine").equals("")
													&& wrapline > 0 && content.length() > wrapline) { // 換行 // modify 12/23
												int offset = 1;
												if (content.matches("^\\w+$")) { // 若字串皆為英文或數字 換行字數*2
													offset = 2;
												}
												content = contentWrap(content, Integer.parseInt(itemElement.getAttribute("wrapLine")) * offset);
												System.out.println(content);
												String[] contentArray = content.split("\n");
												canvas.endText();
												for (int j = 0; j < contentArray.length; j++) {
													
													if(j == 0) {
														x = getXForWrapLine(jmap, itemElement, bf, canvas, contentArray[j], itemFontSize);
														y = getYForWrapLine(jmap, itemElement, bf, canvas, contentArray[j], contentArray.length);
													}
													
													canvas.beginText();
													if (itemElement.getAttribute("horAlign").equals("right")) { // 靠右上換行
														// 設定文字位置
														canvas.setTextMatrix(x + ((contentArray.length - 1) * itemFontSize),
																canvas.getPdfDocument().getPageSize().getHeight() - (y + (j * itemFontSize)));
														canvas.setRGBColorFill(0, 0, 80); // 顏色更改
														canvas.showText(contentArray[j]); // set text --from json
														canvas.endText();
														continue;
													}
													canvas.setTextMatrix(x, canvas.getPdfDocument().getPageSize().getHeight()
															- (y + (j * itemFontSize))); // 設定文字位置kaiuFontSize
													canvas.showText(contentArray[j]); // set text --from json
													canvas.endText();
												}
												continue;
											}
											
											// 為了付款授權書所做的特殊處理
											if ("payment".equals(itemElement.getAttribute("menuId"))
													&& "creditCard_first".equals(itemElement.getAttribute("itemId"))) {
												content = unbn600FirstPaymentChecked;
											}

											if ("payment".equals(itemElement.getAttribute("menuId"))
													&& "creditCard_renewal".equals(itemElement.getAttribute("itemId"))) {
												content = unbn600RenewalPaymentChecked;
											}
											// end 
											
											canvas.setTextMatrix(x, canvas.getPdfDocument().getPageSize().getHeight() - y); // 設定文字位置, (0,0)為左下角開始
											canvas.showText(content); // set text --from json
											
										}
										
									}
																											
								}
								
								canvas.endText();

//								// Amy Hung: 想辨法讓它換行
//								Font font = new Font(normalFont);
//								ColumnText ct2 = new ColumnText(canvas);
//								font.setSize(7);
//								Chunk c1 = new Chunk("身故保險金受益人：（被保險人）\n姓名：全大寶 國籍:中華民國 出生日期：99.5.5 身分證字號:F122442222 與被保險人關係:子女 順位：3比例：34% 聯絡地址\\/電話：台北市信義區市民大道六段市民巷288號之15十二樓／02-22223333\n姓名：全中寶 國籍:中華民國 出生日期：97.5.5 身分證字號: F112121212 與被保險人關係:子女 順位：3比例：33% 聯絡地址\\/電話：110台北市信義區市民大道六段市民巷288號之15十二樓／02-22223333\n姓名：全小寶 國籍:中華民國 出生日期：97.5.5 身分證字號: F112121212 與被保險人關係:子女 順位：3比例：33% 聯絡地址\\/電話：110台北市信義區市民大道六段市民巷288號之15十二樓／02-22223333\\n", font);
//								
//								Phrase p1 = new Phrase(c1);
//								ct2.setSimpleColumn(p1, 134, 200, 318, 300, 9, com.itextpdf.text.Element.ALIGN_JUSTIFIED);
//
//								ct2.go();

							}
							try {
								stamper.setFormFlattening(false);
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
								throw new Exception("PDF錯誤 錯誤檔案:" + pathMap.get("original_PDF_path"));
							}
							
							// pageNum++;
							pathMap = createPathMap(jmap, p, newPolicyNo, g, subPath);
							reader = new PdfReader(pathMap.get("original_PDF_path")); // input PDF path
							
						}
						
					} // end of pdf pageNumber
					
				} // end for pageId
				
			} // end for 主約
						
			return pathList; // 回傳填寫完成的PDF pathList
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* 生成pdf所需路徑Map */
	private HashMap createPathMap(JsonNode jmap, int p, String newPolicyNo, int g, String subPath) throws Exception {
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
			throw new Exception("Mobile Insured Error: Fail when generating pathMap.");
			// return null;
		}
	}

	/* 取得XML NodeLsit */
	private static NodeList getNodeList(String settingXmlPath) throws Exception {
		try {
			File xml = new File(settingXmlPath.replace("", "")); // xml path
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xml);
			doc.getDocumentElement().normalize();
			return doc.getElementsByTagName("item"); // 取得所有item標籤名 回傳Nodelist
		} catch (Exception e) {
			throw new Exception(e.getMessage());
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
	private static float getX(JsonNode jmap, Element itemElement, BaseFont bf, PdfContentByte canvas, String s, float fontSize) {
		String fieldContent = jmap
				.at("/" + itemElement.getAttribute("menuId") + "/" + s + itemElement.getAttribute("itemId")).asText();
		float kaiuFontSize = fontSize;
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
		float start = (Float.parseFloat(itemElement.getAttribute("y")+1) / 100)
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

		//canvas.setFontAndSize(normalFont, kaiuFontSize);

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

	/** 2018.12.27 added 
	 * WrapLine 的時候X, Y座標需要另外計算
	 * 取得文字開始的X座標 */
	private static float getXForWrapLine(JsonNode jmap, Element itemElement, BaseFont bf, PdfContentByte canvas, String fieldContent, float fontSize) {
				float kaiuFontSize = fontSize;
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
	
	/** 2018.12.27 added 
	 * WrapLine 的時候X, Y座標需要另外計算
	 * 取得文字開始的Y座標 */
	private static float getYForWrapLine(JsonNode jmap, Element itemElement, BaseFont bf, PdfContentByte canvas, String fieldContent, int length) {

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
			return (start + end) / 2 + offset * (1 - (length - 1));
		}
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
