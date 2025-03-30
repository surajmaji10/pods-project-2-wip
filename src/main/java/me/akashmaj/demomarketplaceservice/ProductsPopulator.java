package me.akashmaj.demomarketplaceservice;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductsPopulator {

    public static int products;
    public static List<Integer> productIds;
    public static List<String> productNames;
    public static List<String> productDescriptions;
    public static List<Integer> productPrices;
    public static List<Integer> productStockQuantitys;

    public ProductsPopulator(){
        products = 0;
        this.productIds = new ArrayList<>();
        this.productNames = new ArrayList<>();
        this.productDescriptions = new ArrayList<>();
        this.productPrices = new ArrayList<>();
        this.productStockQuantitys = new ArrayList<>();
    }

    public void processExcelFile() {

        try (FileInputStream fileInputStream = new FileInputStream("./src/main/resources/products.xlsx")) {

            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            // skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) continue;

                // Get cell values
                Cell idCell = row.getCell(0);
                Cell nameCell = row.getCell(1);
                Cell descriptionCell = row.getCell(2);
                Cell priceCell = row.getCell(3);
                Cell stockQuantityCell = row.getCell(4);


                // Convert Excel model to Product entity
                Integer id = (int)(idCell.getNumericCellValue());
                String name = nameCell.getStringCellValue();
                String description = descriptionCell.getStringCellValue();
                Integer price = (int)(priceCell.getNumericCellValue());
                Integer stockQuantity = (int)(stockQuantityCell.getNumericCellValue());

                this.productIds.add(id);
                this.productNames.add(name);
                this.productDescriptions.add(description);
                this.productPrices.add(price);
                this.productStockQuantitys.add(stockQuantity);

                products += 1;
            }
            System.out.printf("Products Populated Successfully");
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
