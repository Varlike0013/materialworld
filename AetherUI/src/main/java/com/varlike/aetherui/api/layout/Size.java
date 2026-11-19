package com.varlike.aetherui.api.layout;

public sealed interface Size {

    record Fixed(float value) implements Size {
        public Fixed {
            if (value < 0) throw new IllegalArgumentException("Fixed size must be >= 0");
        }
    }

    record WrapContent() implements Size {}

    record FillParent() implements Size {}

    Size WRAP = new WrapContent();
    Size FILL = new FillParent();

    static Size fixed(float v) { return new Fixed(v); }
}