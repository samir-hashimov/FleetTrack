package com.fleettrack.service;

import com.fleettrack.dao.repository.VehicleRepository;
import com.fleettrack.dto.response.MaintenanceLogResponse;
import com.fleettrack.util.VehicleStatus;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FleetReportPdfService {

    private final VehicleRepository vehicleRepository;
    private final MaintenanceLogService maintenanceLogService;

    public byte[] generateFleetStatusReport() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("FleetTrack - Fleet Status Report", titleFont));
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Fleet Summary", headerFont));
            document.add(new Paragraph(String.format("Total Vehicles: %d", vehicleRepository.count()), normalFont));
            document.add(new Paragraph(String.format("Active Vehicles: %d", vehicleRepository.countByStatus(VehicleStatus.ACTIVE)), normalFont));
            document.add(new Paragraph(String.format("In Maintenance: %d", vehicleRepository.countByStatus(VehicleStatus.IN_MAINTENANCE)), normalFont));
            document.add(new Paragraph(String.format("Offline Vehicles: %d", vehicleRepository.countByStatus(VehicleStatus.OFFLINE)), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Maintenance Logs", headerFont));
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            addTableHeader(table, "Vehicle", "Description", "Scheduled", "Completed");

            List<MaintenanceLogResponse> logs = maintenanceLogService.getAllAsList();
            for (MaintenanceLogResponse log : logs) {
                table.addCell(cell(log.getVehicleLicensePlate(), normalFont));
                table.addCell(cell(log.getDescription(), normalFont));
                table.addCell(cell(log.getScheduledDate().toString(), normalFont));
                table.addCell(cell(log.isCompleted() ? "Yes" : "No", normalFont));
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new com.fleettrack.exception.BusinessException("Failed to generate PDF report: " + e.getMessage());
        } catch (Exception e) {
            throw new com.fleettrack.exception.BusinessException("Failed to generate PDF report: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private PdfPCell cell(String text, Font font) {
        return new PdfPCell(new Phrase(text, font));
    }
}
