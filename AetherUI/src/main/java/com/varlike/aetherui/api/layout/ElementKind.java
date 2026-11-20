package com.varlike.aetherui.api.layout;

public enum ElementKind {
    PANEL(true), ROW(true), COLUMN(true), TABLE(true), CANVAS(true),
    LABEL(false), ICON(false),STACK(true);

    private final boolean container;

    ElementKind(boolean container) { this.container = container; }

    public boolean isContainer() { return container; }
}