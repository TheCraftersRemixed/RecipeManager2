package haveric.recipeManager.flags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import haveric.recipeManager.Messages;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManager.tools.ToolsItem;

public class FlagHoldItem extends Flag {

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <item or false>", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Makes the recipe require crafter to hold an item.",
            "",
            "This flag can be used more than once to add more items, the player will need to hold one to craft.",
            "",
            "The <item> argument can be in this format: material:data:amount",
            "Just like recipe results, not all values from the item are required.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} iron_pickaxe // any data/damage value",
            "{flag} iron_axe:0 // only undamaged axe!",
            "{flag} chainmail_helmet | protection_fire:1 // requires chain helmet with any level of damage and fire protection enchant level 1",
            "{flag} false // makes all previous statements useless", };
    }


    private List<ItemStack> items = new ArrayList<ItemStack>();
    private String failMessage;

    public FlagHoldItem() {
    }

    public FlagHoldItem(FlagHoldItem flag) {
        for (ItemStack i : flag.items) {
            items.add(i.clone());
        }

        failMessage = flag.failMessage;
    }

    @Override
    public FlagHoldItem clone() {
        super.clone();
        return new FlagHoldItem(this);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setItems(List<ItemStack> newItems) {
        items = newItems;
    }

    public void addItem(ItemStack item) {
        items.add(item);
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

        value = split[0].trim();

        ItemStack item = Tools.parseItem(value, Short.MAX_VALUE);

        if (item == null) {
            return false;
        }

        addItem(item);

        return true;
    }

    @Override
    protected void onCheck(Args a) {
        StringBuilder s = new StringBuilder();
        boolean found = false;

        if (a.hasPlayer()) {
            ItemStack held = a.player().getItemInHand();

            if (held != null) {
                for (int i = 0; i < items.size(); i++) {
                    if (ToolsItem.isSimilarDataWildcard(items.get(i), held)) {
                        found = true;
                        break;
                    }

                    if (i > 0) {
                        s.append(", ");
                    }

                    s.append(ToolsItem.print(items.get(i)));
                }
            }
        }

        if (!found) {
            a.addReason(Messages.FLAG_HOLDITEM, failMessage, "{items}", s.toString());
        }
    }
}
