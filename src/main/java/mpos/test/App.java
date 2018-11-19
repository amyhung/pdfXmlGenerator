package mpos.test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

public class App {

	public static final String xmlFilePath = "C:\\Users\\Weu\\Desktop\\UNBA02551.xml";

	public  void main(String[] args) throws IOException, ParserConfigurationException, TransformerException {

		System.out.println("Hello World!2222222");
		// you only need a PdfStamper if you're going to change the existing PDF.
		// PdfReader reader = new PdfReader("C:\\Users\\Weu\\Desktop\\UNBA04077.pdf");

		PdfReader reader = new PdfReader("C:\\Users\\Weu\\Desktop\\UNBA02551.pdf");

		AcroFields fields = reader.getAcroFields();
		fields.getFieldPositions("");

		Set<String> fldNames = fields.getFields().keySet();

		for (String fldName : fldNames) {
			System.out.println(fldName + ": " + fields.getField(fldName));
		}

		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element root = document.createElement("items");
		document.appendChild(root);

		for (String fName : fldNames) {

			List<AcroFields.FieldPosition> positions = fields.getFieldPositions(fName);
			String fieldName = fields.getField(fName);
			Rectangle rect = positions.get(0).position; // In points:
			float left = rect.getLeft();
			float bTop = rect.getTop();
			float width = rect.getWidth();
			float height = rect.getHeight();

			int page = positions.get(0).page;
			Rectangle pageSize = reader.getPageSize(page);
			float pageHeight = pageSize.getTop();
			float top = pageHeight - bTop;

			float x = left;
			float y = bTop;
			float w = left + width;
			float h = top + height;

			x = (float) (x * 0.0352778);
			y = (float) (y * 0.0352778);
			w = (float) (w * 0.0352778);
			h = (float) (h * 0.0352778);

			y = (float) (29.7 - y);

			x = x / 21 * 100;
			y = (float) (y / 29.7 * 100);
			w = w / 21 * 100;
			h = (float) (h / 29.7 * 100);

			DecimalFormat df = new DecimalFormat("##.00");
			x = Float.parseFloat(df.format(x));
			y = Float.parseFloat(df.format(y));
			w = Float.parseFloat(df.format(w));
			h = Float.parseFloat(df.format(h));

			Element item = document.createElement("item");
			String[] aryField = fName.split(":");
			item.setAttribute("menuId", aryField[0]);
			item.setAttribute("itemId", aryField[1]);
			item.setAttribute("x", String.valueOf(x));
			item.setAttribute("y", String.valueOf(y));
			item.setAttribute("w", String.valueOf(w));
			item.setAttribute("h", String.valueOf(h));
			root.appendChild(item);

			System.out.print(fName + ", " + page + ", x:" + x + ", y:" + y + ", w:" + w + ", h:" + h + "\n");
			System.out.println("");

		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(document);
		StreamResult streamResult = new StreamResult(new File(xmlFilePath));
		transformer.transform(domSource, streamResult);
		System.out.println("Done creating XML File");

	}

}
