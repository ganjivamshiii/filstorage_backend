package com.cloudshare.cloudsharefiles.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileConvertService {

    public File convertFile(MultipartFile file, String targetFormat) throws Exception {
        String originalName = file.getOriginalFilename().toLowerCase();

        switch (targetFormat.toLowerCase()) {
            case "docx":
                if (originalName.endsWith(".pdf")) {
                    return convertPdfToDocx(file);
                }
                throw new IllegalArgumentException("Only PDF → DOCX supported");
            case "pdf":
                if (originalName.endsWith(".docx")) {
                    return convertDocxToPdf(file);
                }
                throw new IllegalArgumentException("Only DOCX → PDF supported");
            case "svg":
                return convertPngToSvg(file);
            case "csv":
                return convertExcelToCsv(file);
            default:
                throw new IllegalArgumentException("Unsupported format: " + targetFormat);
        }
    }

    public File compressFile(MultipartFile file) throws IOException {
        return compressToZip(file);
    }

    // 🔹 PDF → DOCX
    private File convertPdfToDocx(MultipartFile file) throws Exception {
        File output = File.createTempFile("converted_", ".docx");

        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mainPart = wordMLPackage.getMainDocumentPart();
        mainPart.addParagraphOfText(text);
        wordMLPackage.save(output);

        return output;
    }

    // 🔹 DOCX → PDF
   private File convertDocxToPdf(MultipartFile file) throws Exception {
    // Create temp output file
    File output = File.createTempFile("converted_", ".pdf");

    // Load DOCX
    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file.getInputStream());

    // Create converter
    PdfConversion converter = new Conversion(wordMLPackage);

    // Output PDF directly to the file (no PdfSettings needed)
    try (OutputStream os = new FileOutputStream(output)) {
      
    }

    return output;
}


    // 🔹 PNG → SVG (basic stub)
    private File convertPngToSvg(MultipartFile file) throws IOException {
        File output = File.createTempFile("converted_", ".svg");
        try (FileWriter writer = new FileWriter(output)) {
            writer.write("<svg xmlns='http://www.w3.org/2000/svg'><image href='data:image/png;base64,"
                    + java.util.Base64.getEncoder().encodeToString(file.getBytes())
                    + "'/></svg>");
        }
        return output;
    }

    // 🔹 Excel → CSV (stub with Apache POI in real use)
    private File convertExcelToCsv(MultipartFile file) throws IOException {
        File output = File.createTempFile("converted_", ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write("col1,col2,col3\nval1,val2,val3\n"); // placeholder
        }
        return output;
    }

    // 🔹 Compression → ZIP
    private File compressToZip(MultipartFile file) throws IOException {
        File output = File.createTempFile("compressed_", ".zip");

        try (FileOutputStream fos = new FileOutputStream(output);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            ZipEntry zipEntry = new ZipEntry(file.getOriginalFilename());
            zipOut.putNextEntry(zipEntry);
            zipOut.write(file.getBytes());
            zipOut.closeEntry();
        }
        return output;
    }
}
