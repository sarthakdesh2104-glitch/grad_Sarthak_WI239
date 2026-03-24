public class Site {
    private int siteId;
    private String siteType;
    private int length;
    private int width;
    private int sizeSqft;
    private double pricePerSqft;
    private boolean isOwned;
    private int ownerId;
    private double remainingMaintenance;

    public Site(int siteId, String siteType, int length, int width, boolean isOwned, int ownerId,double pricePerSqft, double remainingMaintenance) {
        this.siteId = siteId;
        this.siteType = siteType;
        this.length = length;
        this.width = width;
        this.sizeSqft = length * width;
        this.pricePerSqft = pricePerSqft;
        this.isOwned = isOwned;
        this.ownerId = ownerId;
        this.remainingMaintenance = remainingMaintenance;
    }

    public double calculateMaintenanceRate() {
        return this.sizeSqft * this.pricePerSqft;
    }
    
    public int getSiteId() { return siteId; }
    public String getSiteType() { return siteType; }
    public int getSizeSqft() { return sizeSqft; }
    public double getPricePerSqft() { return pricePerSqft; }
    public int getLength() { return length; }
    public int getWidth() { return width; }
    public boolean getIsOwned() { return isOwned; }
    public int getOwnerId() { return ownerId; }
    public double getRemainingMaintenance() { return remainingMaintenance; }
}