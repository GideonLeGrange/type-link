package rec;

import java.time.LocalDate;

public record Invoice(Long id, Long clientId, LocalDate invoiceDate, String description, double amount, boolean paid) {

    public Invoice(Long id, Long clientId, LocalDate invoiceDate, String description, double amount) {
        this(id, clientId, invoiceDate, description, amount, false);
    }

}
