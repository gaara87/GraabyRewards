package graaby.app.wallet.events;

/**
 * Created by Akash on 3/3/15.
 */
public class ToolbarEvents {
    private final int tbBgColor;
    private final int sbBgColor;

    public ToolbarEvents(int tbBgColor, int sbBgColor) {
        this.sbBgColor = sbBgColor;
        this.tbBgColor = tbBgColor;
    }

    public int getToolbarBgColor() {
        return tbBgColor;
    }

    public int getSbBgColor() {
        return sbBgColor;
    }

}
