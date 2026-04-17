package com.spx.services;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

// Service responsible for creating Excel workbooks in memory
@Service
public class ExcelGeneratorService {

    /**
     * Generates an Excel workbook in memory and returns it as a byte array.
     * The caller must pass a lambda or method reference that receives
     * the workbook and is responsible for adding sheets, rows, cells,
     *  styles, charts, and any other Excel content.
     *  Once the workbook has been fully populated, write its binary
     *  content into an in-memory output stream.
     *  At this point, the Excel structure is converted into the raw
     *  bytes of a valid .xlsx file.
     *
     * @param workbookContentGenerator the logic that fills the workbook
     * @return the generated Excel file as a byte array
     */
    public byte[] generateWorkbook(Consumer<XSSFWorkbook> workbookContentGenerator) {

        try
                (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                workbookContentGenerator.accept(workbook);

                workbook.write(outputStream);

            /*
             * Return the final Excel file content as a byte array.
             * This allows the caller to:
             * - save the Excel file to disk
             * - attach it to an email
             * - return it from an API endpoint
             */
                return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate Excel workbook.", exception);
        }
    }
}