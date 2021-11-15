package nl.enjarai.multichats.commands;

import nl.enjarai.multichats.MultiChats;

public enum ModificationType {
    SET_OWNER("player", false, MultiChats.CONFIG.messages.modifyOwner),
    ADD_MANAGER("player", false, MultiChats.CONFIG.messages.modifyAddManager),
    REMOVE_MANAGER("player", false, MultiChats.CONFIG.messages.modifyRemoveManager),
    PREFIX("string", false, MultiChats.CONFIG.messages.modifyPrefix),
    PREFIX_RESET(null, false, MultiChats.CONFIG.messages.resetPrefix),
    SETHOME(null, false, MultiChats.CONFIG.messages.homeSet),
    UNSETHOME(null, false, MultiChats.CONFIG.messages.homeUnset),
    DISPLAY_NAME("string", true, MultiChats.CONFIG.messages.modifyDisplayName),
    DISPLAY_NAME_SHORT("string", true, MultiChats.CONFIG.messages.modifyDisplayNameShort),
    RENAME("string", false, MultiChats.CONFIG.messages.modifyRenamed);

    public final String argumentType;
    public final boolean formatStringArg;
    public final String message;

    ModificationType(String argumentType, boolean formatStringArg, String message) {

        this.argumentType = argumentType;
        this.formatStringArg = formatStringArg;
        this.message = message;
    }
}
