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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import javax.xml.parsers.ParserConfigurationException;
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
					processGenerator();
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
				txtLog.append("XML �x�s���|: " + file.getPath() + "." + newline);
			} else {
				txtLog.append("Open command cancelled by user." + newline);
			}
			txtLog.setCaretPosition(txtLog.getDocument().getLength());
		}

	}

	/**
	 * ���� XML �M���ɮת��x�s�W��
	 * 
	 * @return
	 */
	public String genXmlSaveFileName() {

		String pdfName = Paths.get(this.txtPdfPath.getText()).getFileName().toString();
		String xmlName = pdfName.split(Pattern.quote("."))[0];

		return xmlName + ".xml";

	}

	/**
	 * �� PDF ���� XML �M���ɮ� A4 �Ȫ� 29.7, A4�ȼe 21
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

			// �v�@���� pdf �ɤ��Ҧ������쪺�M�� XML item
			for (String fName : fldNames) {

				// check ���W�٬O�_���� field ���W�٩w�q�W�d�G
				// �H�u:�v���Ϲj�A�զX�U���]�w�ѼơG
				// 1-��Ƹs�ձƧ�:�Ʀr�A�̷ӼƦr���ǲ��� xml �`�I
				// 2-menuId
				// 3-itemId
				// 4-������ƹ���覡(R:�a�k, L�G�a��)
				// 5-������ƹ���覡(T:�a�W, M:�m��)
				// PS�G��ƹ���覡�i�H���]
				String[] formField = fName.split("#");
				String horAlign = "";
				String verAlign = "";
				if (formField.length < 3) {
					System.out.println("��� " + fName + " �S�����ӳW�d�w�q���n�ѼơA�����N���|�Q���ͩ� XML �ɤ�");
					continue;
				} else if (formField.length >= 4) {
					if (!"R".equals(formField[3]) && !"L".equals(formField[3])) {
						System.out.println("��� " + fName + " ��������ƹ���覡�]�w���~�A���T���� R �� L, �]�w�Ȭ� " + formField[3]);
						continue;
					}
					if (formField.length == 5 && !"T".equals(formField[4]) && !"B".equals(formField[4])) {
						System.out.println("��� " + fName + " ��������ƹ���覡�]�w���~�A���T���� T �� B, �]�w�Ȭ� " + formField[4]);
						continue;
					}
					horAlign = formField.length >= 4 ? formField[3] : "";
					verAlign = formField.length >= 5 ? formField[4] : "";
				}
				if (fields.getField(fName).trim().length() == 0) {
					System.out.println("��� " + fName + " �S���w�q�������W��, �����N���|�Q���ͩ� XML �ɤ�");
					continue;
				}
				// end check

				List<AcroFields.FieldPosition> positions = fields.getFieldPositions(fName);

				Rectangle fieldRect = positions.get(0).position; // In points
				float left = fieldRect.getLeft();
				float bottomTop = fieldRect.getTop(); // itext ���O�q���U��_
				float width = fieldRect.getWidth();
				float height = fieldRect.getHeight();

				int page = positions.get(0).page;
				Rectangle pageSize = reader.getPageSize(page);
				float pageHeight = pageSize.getTop();
				float top = pageHeight - bottomTop; // �M���ɮ׬O�q���W��_�A�H���h��U�A���⦨���W��m

				float x = left;
				float y = top; // bottomTop;
				float w = left + width;
				float h = top + height;

				// �� 0.0352778 �O�]�� positions ���O points�A�n�⥦�ର cm
				x = (float) (x * 0.0352778);
				y = (float) (y * 0.0352778);
				w = (float) (w * 0.0352778);
				h = (float) (h * 0.0352778);

				// A4 �Ȫ� 29.7, A4�ȼe 21
				// �� 29.7 ��h y �O�]�� itext �� y �O���U��_�A���O�M���ɮ׬O�n�q���W
				// �ҥH�n�� 29.7 ��h���U��_�� y�A�Y�����W��_�����G
				// y = (float) (29.7 - y);

				// ���⵴���m��
				x = x / 21 * 100;
				y = (float) (y / 29.7 * 100);
				w = w / 21 * 100;
				h = (float) (h / 29.7 * 100);

				// �u���p�ƫ�G��
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

				System.out.print(fName + ", x:" + x + ", y:" + y + ", w:" + w + ", h:" + h + "\n");

			}

			Collections.sort(xmlItems, new SortByOrder());
			generatorXml(xmlItems, xmlSaveName);
			reOrgXmlAttrs(xmlSaveName);

			System.out.println("Done creating XML File");

		} catch (Exception ex) {
			System.out.println("���� XML �M���ɮ׮ɵo�Ϳ��~�I");
			System.out.println(ex);
		}

	}

	/**
	 * ���� Xml �ɮ�
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

		for (XmlItem xmlItem : xmlItems) {

			Element item = document.createElement("item");

			// �]�w Xml �`�I�ݩ�
			// PS: �o�̥[�W A_, B_..�O�]�� xml �b�s�ɮɡA�ݩʦW�٪��ƧǤ��O�ڭn���A�ҥH���[�WA_, ��ӦA���[�u�B�z
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
		DOMSource domSource = new DOMSource(document);
		StreamResult streamResult = new StreamResult(new File(this.txtXmlSavePath.getText() + "\\" + xmlSaveName));
		transformer.transform(domSource, streamResult);

	}

	/**
	 * �����Ψ��ݩʱƧǦӥ[�W���Ȯɩʦr��G<br/>
	 * �]���ק�F xml �ɮצA�x�s�ɡA�ݩʪ����Ƿ|�]���A���F���������ܶ��Ǩ̦����G menuId/itemId/x/y,w,h<br/>
	 * �ҥH�Q�Φb�� xml �ɡA���ݩʦW�٧אּ A_ �}�Y�A�ҥH�o�̭n�A�H��r�ɤ覡���}�A�M���o�ǥ������M��
	 * 
	 * @param saveFileName
	 * @throws IOException
	 */
	private void reOrgXmlAttrs(String saveFileName) throws IOException {

		Path path = Paths.get(this.txtXmlSavePath.getText() + "\\" + saveFileName);
		Charset charset = StandardCharsets.UTF_8;

		// �]���ק�F xml �ɮצA�x�s�ɡA�ݩʪ����Ƿ|�]���A���F���������ܶ��Ǩ̦����G menuId/itemId/x/y,w,h
		// �ҥH�Q�Φb�� xml �ɡA���ݩʦW�٧אּ Remove_x_ �}�Y�A�ҥH�o�̭n�A�H��r�ɤ覡���}�A�M���o�ǥ������M��
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
