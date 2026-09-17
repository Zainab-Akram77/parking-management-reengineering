package parking;

import DataBase.ParkingDataBase;

/**
 * Shared parking check-in logic for both Car and Bike entry screens.
 *
 * Refactoring applied: Extract Class / Pull Up Method.
 * Before: Detail.java (Car) and BikeDetail.java (Bike) were two separate
 * classes with ~95% identical code -- same fields, same layout, same
 * button logic. Only the table name ("Car"/"Bike") and the parking
 * charge (30/20) were different.
 *
 * After: the shared, non-UI logic lives here ONCE. Detail and BikeDetail
 * now extend this class and only supply the two things that actually
 * differ: the vehicle type name and its parking charge.
 *
 * This keeps the original Detail/BikeDetail classes intact (so their
 * NetBeans .form GUI designer files still match), while removing the
 * duplicated business logic.
 */
public abstract class VehicleCheckInBase extends javax.swing.JFrame {

    protected final ParkingDataBase dataBase = new ParkingDataBase();

    /** @return the vehicle type, e.g. "Car" or "Bike" (matches DB table prefix). */
    protected abstract String getVehicleType();

    /** @return the parking charge for this vehicle type. */
    protected abstract int getParkingCharge();

    /**
     * Looks up the next ID and an available position for this vehicle
     * type. Used by the Reset button on both screens.
     */
    protected String getNextId() {
        return String.valueOf(dataBase.getID(getVehicleType() + "Detail"));
    }

    protected String getAvailablePosition() {
        return dataBase.SelectCarPosition(getVehicleType() + "Position");
    }

    /**
     * Parses the ID field safely. Returns -1 if the field is empty or
     * not a valid number, instead of letting NumberFormatException
     * crash the whole application (original bug: no validation existed).
     */
    protected int parseIdSafely(String idText) {
        try {
            return Integer.parseInt(idText.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Saves the parking detail to the database for this vehicle type.
     */
    protected void saveParkingDetail(int id, String regNo, String position) {
        dataBase.InsertCarDetail(getVehicleType(), id, regNo, position);
    }

    /**
     * Builds the parking slip text shown to the staff member / printed
     * on the ticket. Centralizing this avoids the bug where BikeDetail's
     * original slip always printed "Parking Charges: 30" (the car price)
     * regardless of the actual bike charge.
     */
    protected String buildParkingSlipText(int id, String regNo, String position) {
        return "*****Parking Slip*****"
                + "\n ID: " + id
                + "\n RegNo: " + regNo
                + "\n Position: " + position
                + "\n Parking Charges: " + getParkingCharge();
    }
}
