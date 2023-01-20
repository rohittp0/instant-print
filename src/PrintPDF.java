import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.jetbrains.annotations.Nullable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

public class PrintPDF {
    private final PDDocument document;
    private PrintService printService;

    public PrintPDF(String file) throws IOException {
        this.document = Loader.loadPDF(new File("test.pdf"));

        if(printService == null)
            throw new IllegalArgumentException("Invalid printer name or device not found");
    }

    public PrintPDF setPrinter(String name)
    {
        this.printService = findPrintService(name);
        return this;
    }

    PrintPDF print() throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        job.setPrintService(printService);
        job.print();
        return this;
    }

    private static @Nullable PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }

        return printServices[0];
    }
}
