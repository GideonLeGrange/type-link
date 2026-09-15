package bean;

import java.time.LocalDate;
import java.util.Objects;

public class Invoice extends Bean {
    private Long clientId;
    private LocalDate invoiceDate;
    private String description;
    private double amount;
    private boolean paid;

    public Invoice() {
        super();
    }

    public Invoice(Long id, Long clientId, LocalDate invoiceDate, String description, double amount, boolean paid) {
        super(id);
        this.clientId = clientId;
        this.invoiceDate = invoiceDate;
        this.description = description;
        this.amount = amount;
        this.paid = paid;
    }

    public Invoice(Long id, Long clientId, LocalDate invoiceDate, String description, double amount) {
        this(id, clientId, invoiceDate, description, amount, false);
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Invoice invoice = (Invoice) o;
        return Double.compare(invoice.amount, amount) == 0 &&
               paid == invoice.paid &&
               Objects.equals(clientId, invoice.clientId) &&
               Objects.equals(invoiceDate, invoice.invoiceDate) &&
               Objects.equals(description, invoice.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clientId, invoiceDate, description, amount, paid);
    }
}
