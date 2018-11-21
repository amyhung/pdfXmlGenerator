package mpos.pdfXmlGenerator;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;

import bsh.This;

import javax.swing.SwingConstants;

public class Generator extends JFrame {

	private JFrame frmXml;
	private JFileChooser fcPdf;
	private JFileChooser fcXml;
	private JButton openPdfButton;
	static private final String newline = "\n";
	private JTextField txtXmlSavePath;
	private JTextField txtPdfPath;
	private JButton btnGenXml;
	JButton openXmlButton;
	private JScrollPane scrollPane;
	private JTextArea txtLog;
	private JLabel txtGenXmlName;
	private JLabel lblXml;
	private JLabel label_1;
	private JTextField txtPdfName;
	private JLabel lblPageindex;
	private JTextField txtPageIndex;
	private JLabel lblStartdate;
	private JTextField txtStartDate;

	/**
	 * tt Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Generator window = new Generator();
					window.frmXml.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Generator() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		fcPdf = new JFileChooser();
		fcXml = new JFileChooser();

		frmXml = new JFrame();
		frmXml.setResizable(false);
		frmXml.setTitle("\u884C\u52D5\u6295\u4FDD XML \u7522\u751F\u5668");
		frmXml.setBounds(100, 100, 745, 678);
		frmXml.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frmXml.getContentPane().setLayout(springLayout);

		openPdfButton = new JButton("\u9078\u53D6 PDF \u6A94\u6848");
		springLayout.putConstraint(SpringLayout.NORTH, openPdfButton, 21, SpringLayout.NORTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, openPdfButton, 34, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, openPdfButton, -591, SpringLayout.SOUTH,
				frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, openPdfButton, -554, SpringLayout.EAST, frmXml.getContentPane());
		openPdfButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) {
				choosePdfPerformed(arg);
			}
		});
		frmXml.getContentPane().add(openPdfButton);
		// frmXml.getContentPane().add(log);

		openXmlButton = new JButton("\u9078\u64C7\u5132\u5B58\u4F4D\u7F6E");
		springLayout.putConstraint(SpringLayout.NORTH, openXmlButton, 16, SpringLayout.SOUTH, openPdfButton);
		springLayout.putConstraint(SpringLayout.WEST, openXmlButton, 33, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, openXmlButton, -546, SpringLayout.SOUTH,
				frmXml.getContentPane());
		openXmlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) {
				chooseXmlSaveLocationPerformed(arg);
			}
		});
		frmXml.getContentPane().add(openXmlButton);

		txtXmlSavePath = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, openXmlButton, -9, SpringLayout.WEST, txtXmlSavePath);
		springLayout.putConstraint(SpringLayout.WEST, txtXmlSavePath, 193, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, txtXmlSavePath, -15, SpringLayout.EAST, frmXml.getContentPane());
		txtXmlSavePath.setBackground(Color.WHITE);
		txtXmlSavePath.setEditable(false);
		txtXmlSavePath.setText("\u9078\u64C7\u7522\u751F\u5B8C\u7684\u7684 XML \u8981\u653E\u54EA\u88E1");
		frmXml.getContentPane().add(txtXmlSavePath);
		txtXmlSavePath.setColumns(10);

		txtPdfPath = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtXmlSavePath, 16, SpringLayout.SOUTH, txtPdfPath);
		springLayout.putConstraint(SpringLayout.WEST, txtPdfPath, 7, SpringLayout.EAST, openPdfButton);
		springLayout.putConstraint(SpringLayout.SOUTH, txtPdfPath, -591, SpringLayout.SOUTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, txtPdfPath, -17, SpringLayout.EAST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, txtPdfPath, 21, SpringLayout.NORTH, frmXml.getContentPane());
		txtPdfPath.setText("\u9078\u64C7\u8981\u7522\u751F XML \u7684 PDF \u6A94\u6848");
		txtPdfPath.setEditable(false);
		txtPdfPath.setColumns(10);
		txtPdfPath.setBackground(Color.WHITE);
		frmXml.getContentPane().add(txtPdfPath);

		btnGenXml = new JButton("\u7522\u751F\u5957\u7248 XML \u6A94");
		springLayout.putConstraint(SpringLayout.EAST, btnGenXml, -34, SpringLayout.EAST, frmXml.getContentPane());
		btnGenXml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (isOK())
						processGenerator();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					txtLog.setText(e.toString());
				}
			}
		});
		frmXml.getContentPane().add(btnGenXml);

		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.SOUTH, btnGenXml, -53, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 22, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 266, SpringLayout.NORTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -17, SpringLayout.SOUTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -20, SpringLayout.EAST, frmXml.getContentPane());
		frmXml.getContentPane().add(scrollPane);

		txtLog = new JTextArea();
		txtLog.setEditable(false);
		scrollPane.setViewportView(txtLog);

		txtGenXmlName = new JLabel("None");
		springLayout.putConstraint(SpringLayout.SOUTH, txtGenXmlName, -10, SpringLayout.NORTH, scrollPane);
		txtGenXmlName.setHorizontalAlignment(SwingConstants.LEFT);
		txtGenXmlName.setForeground(Color.BLUE);
		frmXml.getContentPane().add(txtGenXmlName);

		lblXml = new JLabel("XML \u6A23\u7248\u6A94\u540D\u7A31\uFF1A");
		springLayout.putConstraint(SpringLayout.WEST, txtGenXmlName, 1, SpringLayout.EAST, lblXml);
		springLayout.putConstraint(SpringLayout.WEST, lblXml, 27, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, lblXml, -6, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, lblXml, -570, SpringLayout.EAST, frmXml.getContentPane());
		lblXml.setHorizontalAlignment(SwingConstants.RIGHT);
		lblXml.setForeground(Color.BLUE);
		frmXml.getContentPane().add(lblXml);

		label_1 = new JLabel("\u8981\u4FDD\u6587\u4EF6\u540D\u7A31");
		springLayout.putConstraint(SpringLayout.NORTH, label_1, 27, SpringLayout.SOUTH, openXmlButton);
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		frmXml.getContentPane().add(label_1);

		txtPdfName = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, btnGenXml, 25, SpringLayout.SOUTH, txtPdfName);
		springLayout.putConstraint(SpringLayout.WEST, txtPdfName, 193, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, txtPdfName, -19, SpringLayout.EAST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, txtXmlSavePath, -19, SpringLayout.NORTH, txtPdfName);
		springLayout.putConstraint(SpringLayout.SOUTH, txtPdfName, -499, SpringLayout.SOUTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, txtPdfName, 116, SpringLayout.NORTH, frmXml.getContentPane());
		txtPdfName.setColumns(10);
		txtPdfName.setBackground(Color.WHITE);
		frmXml.getContentPane().add(txtPdfName);

		lblPageindex = new JLabel("PageIndex");
		springLayout.putConstraint(SpringLayout.NORTH, lblXml, 37, SpringLayout.SOUTH, lblPageindex);
		springLayout.putConstraint(SpringLayout.EAST, label_1, 0, SpringLayout.EAST, lblPageindex);
		lblPageindex.setHorizontalAlignment(SwingConstants.RIGHT);
		springLayout.putConstraint(SpringLayout.WEST, lblPageindex, 97, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, lblPageindex, -565, SpringLayout.EAST, frmXml.getContentPane());
		frmXml.getContentPane().add(lblPageindex);

		txtPageIndex = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtGenXmlName, 34, SpringLayout.SOUTH, txtPageIndex);
		springLayout.putConstraint(SpringLayout.SOUTH, txtPageIndex, -445, SpringLayout.SOUTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, txtPageIndex, 19, SpringLayout.EAST, lblPageindex);
		springLayout.putConstraint(SpringLayout.NORTH, lblPageindex, 4, SpringLayout.NORTH, txtPageIndex);
		springLayout.putConstraint(SpringLayout.NORTH, txtPageIndex, 27, SpringLayout.SOUTH, txtPdfName);
		txtPageIndex.setText("1");
		txtPageIndex.setColumns(10);
		txtPageIndex.setBackground(Color.WHITE);
		frmXml.getContentPane().add(txtPageIndex);

		lblStartdate = new JLabel("StartDate");
		springLayout.putConstraint(SpringLayout.NORTH, lblStartdate, 31, SpringLayout.SOUTH, txtPdfName);
		frmXml.getContentPane().add(lblStartdate);

		txtStartDate = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, txtStartDate, 27, SpringLayout.SOUTH, txtPdfName);
		springLayout.putConstraint(SpringLayout.SOUTH, txtStartDate, -68, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, lblStartdate, -8, SpringLayout.WEST, txtStartDate);
		springLayout.putConstraint(SpringLayout.WEST, txtStartDate, 392, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, txtStartDate, -50, SpringLayout.WEST, btnGenXml);
		txtStartDate.setColumns(10);
		txtStartDate.setBackground(Color.WHITE);
		frmXml.getContentPane().add(txtStartDate);

		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String strDate = dateFormat.format(date);
		this.txtStartDate.setText(strDate);

	}

	/**
	 * choose pdf file
	 * 
	 * @param e
	 */
	public void choosePdfPerformed(ActionEvent e) {

		// Handle open button action.
		if (e.getSource() == openPdfButton) {

			FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF ONLY", "pdf", "pdf");
			fcPdf.setFileFilter(filter);

			int returnVal = fcPdf.showOpenDialog(Generator.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fcPdf.getSelectedFile();
				this.txtPdfPath.setText(file.getPath());
				txtLog.append("��� PDF: " + file.getPath() + "." + newline);
			} else {
				txtLog.append("Open command cancelled by user." + newline);
			}
			txtLog.setCaretPosition(txtLog.getDocument().getLength());
			// Handle save button action.
		}

	}

	/**
	 * choose the location of xml file
	 * 
	 * @param e
	 */
	public void chooseXmlSaveLocationPerformed(ActionEvent e) {

		// Handle open button action.
		if (e.getSource() == openXmlButton) {

			fcXml.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fcXml.showOpenDialog(Generator.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fcXml.getSelectedFile();
				this.txtXmlSavePath.setText(file.getPath());
				txtLog.append("XML 儲存路徑: " + file.getPath() + "." + newline);
			} else {
				txtLog.append("Open command cancelled by user." + newline);
			}
			txtLog.setCaretPosition(txtLog.getDocument().getLength());
		}

	}

	/**
	 * 產生 XML 套版檔案的儲存名稱
	 * 
	 * @return
	 */
	public String genXmlSaveFileName() {

		String pdfName = Paths.get(this.txtPdfPath.getText()).getFileName().toString();
		String xmlName = pdfName.split(Pattern.quote("."))[0];

		return xmlName + ".xml";

	}

	/**
	 * 檢查必要欄位
	 * 
	 * @return
	 */
	public boolean isOK() {

		if (this.txtPdfPath.getText().length() == 0 || "選擇要產生 XML 的 PDF 檔案".equals(this.txtPdfPath.getText())) {
			JOptionPane.showMessageDialog(null, "請選擇 PDF 檔案！");
			return false;
		}

		if (this.txtXmlSavePath.getText().length() == 0 || "選擇產生完的的 XML 要放哪裡".equals(this.txtXmlSavePath.getText())) {
			JOptionPane.showMessageDialog(null, "請選擇產生的 XML 檔案要儲存在哪裡！");
			return false;
		}

		if (this.txtPdfName.getText().length() == 0) {
			JOptionPane.showMessageDialog(null, "請輸入要保文件名稱！");
			return false;
		}

		if (this.txtPageIndex.getText().length() == 0) {
			JOptionPane.showMessageDialog(null, "請輸入 PageIndex !");
			return false;
		}

		try {
			Integer.parseInt(this.txtPageIndex.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "PageIndex 必須是數字！");
			return false;
		}

		if (this.txtStartDate.getText().length() == 0) {
			JOptionPane.showMessageDialog(null, "請輸入 StartDate ！");
			return false;
		}

		try {
			Date date1 = new SimpleDateFormat("yyyy/mm/dd").parse(this.txtStartDate.getText());
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(null, "StartDate 不是正確的日期格式！");
			return false;
		}

		this.txtLog.setText("");

		return true;

	}

	/**
	 * 依 PDF 產生 XML 套版檔案 A4 紙長 29.7, A4紙寬 21
	 * 
	 * @throws Exception
	 */
	private void processGenerator() throws Exception {

		try {

			String xmlSaveName = genXmlSaveFileName();
			this.txtGenXmlName.setText(xmlSaveName);

			// read the pdf file.
			PdfReader reader = new PdfReader(this.txtPdfPath.getText());

			AcroFields fields = reader.getAcroFields();
			Set<String> fldNames = fields.getFields().keySet();

			ArrayList<XmlItem> xmlItems = new ArrayList<XmlItem>();

			int genCount = 0;
			int totalCount = 0;

			// 逐一產生 pdf 檔中所有表單欄位的套版 XML item
			for (String fName : fldNames) {

				totalCount++;

				// check 欄位名稱是否有照 field 的名稱定義規範：
				// 以「:」為區隔，組合各項設定參數：
				// 1-資料群組排序:數字，依照數字順序產生 xml 節點
				// 2-menuId
				// 3-itemId
				// 4-水平資料對齊方式(R:靠右, L：靠左)
				// 5-垂直資料對齊方式(T:靠上, M:置中)
				// PS：資料對齊方式可以不設
				String[] formField = fName.split("#");
				String horAlign = "";
				String verAlign = "";
				if (formField.length < 3) {
					txtLog.append("欄位 " + fName + " 沒有按照規範定義必要參數，此欄位將不會被產生於 XML 檔中" + newline);
					continue;
				} else if (formField.length >= 4) {
					if (!"R".equals(formField[3]) && !"L".equals(formField[3])) {
						txtLog.append("欄位 \" + fName + \" 的水平資料對齊方式設定錯誤，正確應為 R 或 L, 設定值為 " + formField[3] + newline);
						continue;
					}
					if (formField.length == 5 && !"T".equals(formField[4]) && !"B".equals(formField[4])) {
						txtLog.append("欄位 \" + fName + \" 的垂直資料對齊方式設定錯誤，正確應為 T 或 B, 設定值為  " + formField[4] + newline);
						continue;
					}
					horAlign = formField.length >= 4 ? formField[3] : "";
					verAlign = formField.length >= 5 ? formField[4] : "";
				}
				if (fields.getField(fName).trim().length() == 0) {
					txtLog.append("欄位 \" + fName + \" 沒有定義中文欄位名稱, 此欄位將不會被產生於 XML 檔中" + newline);
					continue;
				}
				// end check

				List<AcroFields.FieldPosition> positions = fields.getFieldPositions(fName);

				Rectangle fieldRect = positions.get(0).position; // In points
				float left = fieldRect.getLeft();
				float bottomTop = fieldRect.getTop(); // itext 的是從左下算起
				float width = fieldRect.getWidth();
				float height = fieldRect.getHeight();

				int page = positions.get(0).page;
				Rectangle pageSize = reader.getPageSize(page);
				float pageHeight = pageSize.getTop();
				float top = pageHeight - bottomTop; // 套版檔案是從左上算起，以高去減左下，換算成左上位置

				float x = left;
				float y = top; // bottomTop;
				float w = left + width;
				float h = top + height;

				// 乘 0.0352778 是因為 positions 單位是 points，要把它轉為 cm
				x = (float) (x * 0.0352778);
				y = (float) (y * 0.0352778);
				w = (float) (w * 0.0352778);
				h = (float) (h * 0.0352778);

				// A4 紙長 29.7, A4紙寬 21
				// 用 29.7 減去 y 是因為 itext 的 y 是左下算起，但是套版檔案是要從左上
				// 所以要用 29.7 減去左下算起的 y，即為左上算起的結果
				// y = (float) (29.7 - y);

				// 換算絕對位置值
				x = x / 21 * 100;
				y = (float) (y / 29.7 * 100);
				w = w / 21 * 100;
				h = (float) (h / 29.7 * 100);

				// 只取小數後二位
				DecimalFormat df = new DecimalFormat("##.00");
				x = Float.parseFloat(df.format(x));
				y = Float.parseFloat(df.format(y));
				w = Float.parseFloat(df.format(w));
				h = Float.parseFloat(df.format(h));

				XmlItem item = new XmlItem();
				item.setOrder(Integer.parseInt(formField[0]));
				item.setMenuId(formField[1]);
				item.setItemId(formField[2]);
				item.setX(String.valueOf(x));
				item.setY(String.valueOf(y));
				item.setW(String.valueOf(w));
				item.setH(String.valueOf(h));
				item.setHorAlign(horAlign);
				item.setVerAlign(verAlign);
				item.setDesc(fields.getField(fName));
				xmlItems.add(item);

				genCount++;

				txtLog.append("產生 XML 節點 - 欄位:" + fields.getField(fName) + ", " + fName + ", x:" + x + ", y:" + y
						+ ", w:" + w + ", h:" + h + newline);

			}

			Collections.sort(xmlItems, new SortByOrder());
			generatorXml(xmlItems, xmlSaveName);
			reOrgXmlAttrs(xmlSaveName);

			this.txtLog.append("----------------------------------------------------------------------------------" + newline);
			this.txtLog.append("總共偵測到： " + totalCount + "欄位, 共產生 " + genCount + " 個 item 節點於 XML 樣版檔中！" + newline);
			this.txtLog.append("----------------------------------------------------------------------------------" + newline);

			txtLog.append("Done creating XML File" + newline);

		} catch (Exception ex) {
			txtLog.append("產生 XML 發生錯誤..." + newline);
			txtLog.append(ex.toString());
		}

	}

	/**
	 * 產生 Xml 檔案
	 * 
	 * @param xmlItems
	 * @param xmlSaveName
	 * @throws ParserConfigurationException
	 * @throws TransformerConfigurationException
	 */
	private void generatorXml(ArrayList<XmlItem> xmlItems, String xmlSaveName) throws Exception {

		// create a new xml file.
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element root = document.createElement("items");
		document.appendChild(root);

		Element title = document.createElement("title");
		title.setAttribute("name", this.txtPdfName.getText());
		title.setAttribute("pageIndex", this.txtPageIndex.getText());
		title.setAttribute("startDate", this.txtStartDate.getText());
		root.appendChild(title);

		for (XmlItem xmlItem : xmlItems) {

			Element item = document.createElement("item");

			// 設定 Xml 節點屬性
			// PS: 這裡加上 A_, B_..是因為 xml 在存檔時，屬性名稱的排序不是我要的，所以先加上A_, 後來再做加工處理
			item.setAttribute("A_menuId", xmlItem.getMenuId());
			item.setAttribute("B_itemId", xmlItem.getItemId());
			item.setAttribute("C_x", xmlItem.getX());
			item.setAttribute("D_y", xmlItem.getY());
			item.setAttribute("E_w", xmlItem.getW());
			item.setAttribute("F_h", xmlItem.getH());

			if (xmlItem.getHorAlign().length() > 0) {
				if ("R".equals(xmlItem.getHorAlign()))
					item.setAttribute("G_horAlign", "right");
				else if ("L".equals(xmlItem.getHorAlign()))
					item.setAttribute("G_horAlign", "left");
			}

			if (xmlItem.getVerAlign().length() > 0) {
				if ("T".equals(xmlItem.getVerAlign()))
					item.setAttribute("H_verAlign", "top");
				else if ("B".equals(xmlItem.getVerAlign()))
					item.setAttribute("H_verAlign", "bottom");
			}

			item.setAttribute("I_desc", xmlItem.getDesc());

			root.appendChild(item);

		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.VERSION, "no");
		DOMSource domSource = new DOMSource(document);
		StreamResult streamResult = new StreamResult(new File(this.txtXmlSavePath.getText() + "\\" + xmlSaveName));
		transformer.transform(domSource, streamResult);

	}

	/**
	 * 拿掉用來屬性排序而加上的暫時性字串：<br/>
	 * 因為修改了 xml 檔案再儲存時，屬性的順序會跑掉，為了能夠維持顯示順序依次為： menuId/itemId/x/y,w,h<br/>
	 * 所以利用在產 xml 時，把屬性名稱改為 A_ 開頭，所以這裡要再以文字檔方式打開，然後把這些全部都清掉
	 * 
	 * @param saveFileName
	 * @throws IOException
	 */
	private void reOrgXmlAttrs(String saveFileName) throws IOException {

		Path path = Paths.get(this.txtXmlSavePath.getText() + "\\" + saveFileName);
		Charset charset = StandardCharsets.UTF_8;

		// 因為修改了 xml 檔案再儲存時，屬性的順序會跑掉，為了能夠維持顯示順序依次為： menuId/itemId/x/y,w,h
		// 所以利用在產 xml 時，把屬性名稱改為 A_ 開頭，所以這裡要再以文字檔方式打開，然後把這些全部都清掉
		String content = new String(Files.readAllBytes(path), "UTF8");
		content = content.replaceAll("A_", "");
		content = content.replaceAll("B_", "");
		content = content.replaceAll("C_", "");
		content = content.replaceAll("D_", "");
		content = content.replaceAll("E_", "");
		content = content.replaceAll("F_", "");
		content = content.replaceAll("G_", "");
		content = content.replaceAll("H_", "");
		content = content.replaceAll("I_", "");
		Files.write(path, content.getBytes(charset));

	}

}
