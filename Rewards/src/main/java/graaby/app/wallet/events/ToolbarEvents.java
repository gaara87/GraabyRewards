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

    public static class SetTitle {
        private final String name;

        public SetTitle(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
