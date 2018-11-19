package mpos.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

public class pdfXmlFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JFileChooser fc;
	private JButton openPdfButton;
	private JButton saveButton;	
	JTextArea log;
	static private final String newline = "\n";

	/**
	 * Launch the application.
	 */
	public  void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					pdfXmlFrame frame = new pdfXmlFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	


	/**
	 * Create the frame.
	 */
	public pdfXmlFrame() {
		setTitle("\u884C\u52D5\u6295\u4FDD Xml \u7522\u751F\u5668");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 719, 460);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		fc = new JFileChooser();
		
		textField = new JTextField();
		textField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 20;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		contentPane.add(textField, gbc_textField);
		textField.setColumns(10);
		openPdfButton = new JButton("選擇 PDF 檔");
		openPdfButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_actionPerformed(e);
			}
		});
        
		GridBagConstraints gbc_openPdfButton = new GridBagConstraints();
		gbc_openPdfButton.insets = new Insets(0, 0, 5, 5);
		gbc_openPdfButton.gridx = 1;
		gbc_openPdfButton.gridy = 2;
		contentPane.add(openPdfButton, gbc_openPdfButton);
		
		log = new JTextArea(5,20);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		GridBagConstraints gbc_log = new GridBagConstraints();
		gbc_log.insets = new Insets(0, 0, 0, 5);
		gbc_log.gridx = 1;
		gbc_log.gridy = 4;
		contentPane.add(log, gbc_log);
	}
	
	public void do_actionPerformed(ActionEvent e) {
		 
        //Handle open button action.
        if (e.getSource() == openPdfButton) {
            int returnVal = fc.showOpenDialog(pdfXmlFrame.this);
 
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                //This is where a real application would open the file.
                log.append("Opening: " + file.getName() + "." + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
 
        //Handle save button action.
        } else if (e.getSource() == saveButton) {
            int returnVal = fc.showSaveDialog(pdfXmlFrame.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would save the file.
                log.append("Saving: " + file.getName() + "." + newline);
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

}
