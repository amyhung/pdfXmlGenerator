package mpos.pdfXmlGenerator;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
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
import com.itextpdf.text.pdf.PdfReader;

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

	/** tt
	 * Launch the application.
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
		frmXml.setTitle("\u884C\u52D5\u6295\u4FDD XML \u7522\u751F\u5668");
		frmXml.setBounds(100, 100, 719, 378);
		frmXml.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frmXml.getContentPane().setLayout(springLayout);

		openPdfButton = new JButton("\u9078\u53D6 PDF \u6A94\u6848");
		springLayout.putConstraint(SpringLayout.NORTH, openPdfButton, 19, SpringLayout.NORTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, openPdfButton, -669, SpringLayout.EAST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, openPdfButton, -532, SpringLayout.EAST, frmXml.getContentPane());
		openPdfButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) {
				choosePdfPerformed(arg);
			}
		});
		frmXml.getContentPane().add(openPdfButton);
		// frmXml.getContentPane().add(log);

		openXmlButton = new JButton("\u9078\u64C7\u5132\u5B58\u4F4D\u7F6E");
		springLayout.putConstraint(SpringLayout.NORTH, openXmlButton, 19, SpringLayout.SOUTH, openPdfButton);
		springLayout.putConstraint(SpringLayout.SOUTH, openXmlButton, -236, SpringLayout.SOUTH,
				frmXml.getContentPane());
		openXmlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) {
				chooseXmlSaveLocationPerformed(arg);
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, openXmlButton, 32, SpringLayout.WEST, frmXml.getContentPane());
		frmXml.getContentPane().add(openXmlButton);

		txtXmlSavePath = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, openXmlButton, -9, SpringLayout.WEST, txtXmlSavePath);
		springLayout.putConstraint(SpringLayout.EAST, txtXmlSavePath, -18, SpringLayout.EAST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, txtXmlSavePath, 178, SpringLayout.WEST, frmXml.getContentPane());
		txtXmlSavePath.setBackground(Color.WHITE);
		txtXmlSavePath.setEditable(false);
		txtXmlSavePath.setText("\u9078\u64C7\u7522\u751F\u5B8C\u7684\u7684 XML \u8981\u653E\u54EA\u88E1");
		frmXml.getContentPane().add(txtXmlSavePath);
		txtXmlSavePath.setColumns(10);

		txtPdfPath = new JTextField();
		springLayout.putConstraint(SpringLayout.SOUTH, txtPdfPath, -280, SpringLayout.SOUTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, txtXmlSavePath, 11, SpringLayout.SOUTH, txtPdfPath);
		springLayout.putConstraint(SpringLayout.NORTH, txtPdfPath, 16, SpringLayout.NORTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, txtPdfPath, 9, SpringLayout.EAST, openPdfButton);
		springLayout.putConstraint(SpringLayout.EAST, txtPdfPath, 514, SpringLayout.EAST, openPdfButton);
		txtPdfPath.setText("\u9078\u64C7\u8981\u7522\u751F XML \u7684 PDF \u6A94\u6848");
		txtPdfPath.setEditable(false);
		txtPdfPath.setColumns(10);
		txtPdfPath.setBackground(Color.WHITE);
		frmXml.getContentPane().add(txtPdfPath);

		btnGenXml = new JButton("\u7522\u751F\u5957\u7248 XML \u6A94");
		btnGenXml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					genXml();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					txtLog.setText(e.toString());
				}
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnGenXml, 111, SpringLayout.NORTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, txtXmlSavePath, -16, SpringLayout.NORTH, btnGenXml);
		springLayout.putConstraint(SpringLayout.WEST, btnGenXml, 32, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnGenXml, 72, SpringLayout.SOUTH, openXmlButton);
		frmXml.getContentPane().add(btnGenXml);

		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 188, SpringLayout.NORTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -21, SpringLayout.SOUTH, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 34, SpringLayout.WEST, frmXml.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, txtXmlSavePath);
		frmXml.getContentPane().add(scrollPane);

		txtLog = new JTextArea();
		txtLog.setEditable(false);
		scrollPane.setViewportView(txtLog);

		txtGenXmlName = new JLabel("\u6A23\u7248\u6A94\u6848\u540D\u7A31");
		springLayout.putConstraint(SpringLayout.SOUTH, txtGenXmlName, -28, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, txtGenXmlName, -35, SpringLayout.EAST, frmXml.getContentPane());
		frmXml.getContentPane().add(txtGenXmlName);

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
				txtLog.append("選擇 PDF: " + file.getPath() + "." + newline);
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
	 * @return 
	 */
	public String genXmlSaveFileName() {
		
		String pdfName = Paths.get(this.txtPdfPath.getText()).getFileName().toString();
		String xmlName = pdfName.split(Pattern.quote("."))[0];
		
		return xmlName + ".xml";
		
	}
	
	/**
	 * 依 PDF 產生 XML 套版檔案
	 * A4 紙長 29.7, A4紙寬 21
	 * @throws Exception
	 */
	private void genXml() throws Exception {
		
		try {
			
		
			String xmlSaveName = genXmlSaveFileName();
			this.txtGenXmlName.setText(xmlSaveName);			

			// read the pdf file.
			PdfReader reader = new PdfReader(this.txtPdfPath.getText());

			AcroFields fields = reader.getAcroFields();
			Set<String> fldNames = fields.getFields().keySet();

			// print all form field of the pdf file.
			for (String fldName : fldNames) {
				System.out.println(fldName + ": " + fields.getField(fldName));
			}

			// create a new xml file.
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element root = document.createElement("items");
			document.appendChild(root);

			// 逐一產生 pdf 檔中所有表單欄位的套版 xml item
			for (String fName : fldNames) {

				List<AcroFields.FieldPosition> positions = fields.getFieldPositions(fName);
				
				//PdfDictionary dict = fields.getFieldItem(fName).getMerged(idx)
				//form.getFieldItem("personal.password").getMerged(0).put(PdfName.Q, new PdfNumber(PdfFormField.Q_RIGHT));
				
				Rectangle fieldRect = positions.get(0).position; // In points
				float left = fieldRect.getLeft();
				float bottomTop = fieldRect.getTop(); //  itext 的是從左下算起
				float width = fieldRect.getWidth();
				float height = fieldRect.getHeight();

				int page = positions.get(0).page;
				Rectangle pageSize = reader.getPageSize(page);
				float pageHeight = pageSize.getTop();
				float top = pageHeight - bottomTop; // 套版檔案是從左上算起，以高去減左下，換算成左上位置

				float x = left;
				float y = top; //bottomTop;
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
				//y = (float) (29.7 - y);

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

				Element item = document.createElement("item");
				String[] aryField = fName.split(":");
				
				// 設定 xml 節點屬性
				item.setAttribute("menuId", aryField[0]);
				item.setAttribute("itemId", aryField[1]);
				item.setAttribute("x", String.valueOf(x));
				item.setAttribute("y", String.valueOf(y));
				item.setAttribute("w", String.valueOf(w));
				item.setAttribute("h", String.valueOf(h));
				root.appendChild(item);

				System.out.print(fName + ", " + page + ", x:" + x + ", y:" + y + ", w:" + w + ", h:" + h + "\n");
				//System.out.println("");

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(new File(this.txtXmlSavePath.getText() + "\\" + xmlSaveName));
			transformer.transform(domSource, streamResult);
			
			System.out.println("Done creating XML File");
			
		} catch (Exception ex) {
			System.out.println("產生 XML 套版檔案時發生錯誤！");
			System.out.println(ex);
		}

	}

}
