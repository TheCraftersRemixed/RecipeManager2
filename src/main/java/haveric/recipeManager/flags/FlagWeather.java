package haveric.recipeManager.flags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.Messages;
import haveric.recipeManagerCommon.util.RMCUtil;

public class FlagWeather extends Flag {

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <type>, [type] | [fail message]", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Sets the weather type(s) required to allow crafting.",
            "Using this flag more than once will overwrite the previous one.",
            "",
            "The 'type' argument can be:",
            "  clear    = clear skies, no precipitation.",
            "  downfall = precipitation (rain/snow depends on biome).",
            "  thunder  = precipitation + thundering.",
            "You can set more than one type separated by , character, but setting all of them is pointless.",
            "",
            "Optionally you can set the 'fail message' argument to overwrite the failure message or set it to 'false' to hide it.",
            "In the fail message you can use the following variables:",
            "  {weather} = the weather type required.",
            "",
            "NOTE: If you need to check if it's raining or snowing then use the " + FlagType.TEMPERATURE + " flag.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} downfall // works only if it's raining peacefully.",
            "{flag} clear, thunder | <red>To be struck by lightning... or to be not.", };
    }


    public class Bit {
        public static final byte CLEAR = 1 << 0;
        public static final byte DOWNFALL = 1 << 1;
        public static final byte THUNDER = 1 << 2;
    }

    private byte weather;
    private String failMessage;

    public FlagWeather() {
    }

    public FlagWeather(FlagWeather flag) {
        weather = flag.weather;
        failMessage = flag.failMessage;
    }

    @Override
    public FlagWeather clone() {
        super.clone();
        return new FlagWeather(this);
    }

    /**
     * Get the set weather requirement.
     *
     * @return 0 = none, 1 = rain/snow, 2 = thunder
     */
    public byte getWeather() {
        return weather;
    }

    /**
     * Set the weather requirement.
     *
     * @param weather
     *            0 = none, 1 = rain/snow, 2 = thunder
     */
    public void setWeather(int newWeather) {
        weather = (byte) newWeather;
    }

    public String getWeatherString() {
        List<String> list = new ArrayList<String>(3);

        if ((weather & Bit.CLEAR) == Bit.CLEAR) {
            list.add("clear");
        }

        if ((weather & Bit.DOWNFALL) == Bit.DOWNFALL) {
            list.add("downfall");
        }

        if ((weather & Bit.THUNDER) == Bit.THUNDER) {
            list.add("thunder");
        }

        return RMCUtil.collectionToString(list);
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String newFailMessage) {
        failMessage = newFailMessage;
    }

    @Override
    protected boolean onParse(String value) {
        String[] split = value.split("\\|");

        if (split.length > 1) {
            setFailMessage(split[1].trim());
        }

        split = split[0].toLowerCase().split(",");

        for (String arg : split) {
            arg = arg.trim();

            switch (arg.charAt(0)) {
                case 'c':
                case 'n':
                    weather |= Bit.CLEAR;
                    break;

                case 'd':
                case 'r':
                case 's':
                    weather |= Bit.DOWNFALL;
                    break;

                case 't':
                case 'l':
                    weather |= Bit.THUNDER;
                    break;

                default:
                    ErrorReporter.warning("Flag " + getType() + " has unknown weather type: " + value);
            }
        }

        if (weather == 0) {
            ErrorReporter.error("Flag " + getType() + " needs at least one valid weather type!");
            return false;
        }

        return true;
    }

    @Override
    protected void onCheck(Args a) {
        if (!a.hasLocation()) {
            a.addCustomReason("Needs location!");
            return;
        }

        World world = a.location().getWorld();
        byte checkWeather;
        if (world.hasStorm()) {
            if (world.isThundering()) {
                checkWeather = Bit.THUNDER;
            } else {
                checkWeather = Bit.DOWNFALL;
            }
        } else {
            checkWeather = Bit.CLEAR;
        }

        if ((getWeather() & checkWeather) != checkWeather) {
            a.addReason(Messages.FLAG_WEATHER, failMessage, "{weather}", getWeatherString());
        }
    }

    /*
     * @Override public List<String> information() { List<String> list = new ArrayList<String>(1);
     *
     * list.add(Messages.FLAG_WEATHER.get("{weather}", getWeatherString()));
     *
     * return list; }
     */
}
