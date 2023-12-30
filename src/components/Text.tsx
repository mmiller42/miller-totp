import React from "react";
import {
  ColorSchemeName,
  Text as _Text,
  TextProps as _TextProps,
  useColorScheme,
} from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";

export type TextColor = "default" | "contrast" | "muted" | "primary";
export type TextWeight = "normal" | "bold" | "bolder";

export type TextStyle = Omit<
  _TextProps["style"],
  "color" | "fontSize" | "fontWeight" | "fontStyle"
>;

export type TextProps = Omit<_TextProps, "style"> & {
  color?: TextColor | undefined;
  size?: number | undefined;
  weight?: TextWeight | undefined;
  italic?: boolean | undefined;
  style?: TextStyle | undefined;
};

type NonNilColorScheme = Extract<ColorSchemeName, string>;

const TEXT_COLORS: Record<NonNilColorScheme, Record<TextColor, string>> = {
  dark: {
    default: Colors.lighter,
    contrast: Colors.white,
    muted: Colors.light,
    primary: Colors.primary,
  },
  light: {
    default: Colors.darker,
    contrast: Colors.black,
    muted: Colors.dark,
    primary: Colors.primary,
  },
} as const;

const BASE_TEXT_SIZE = 18;

const TEXT_WEIGHTS: Record<TextWeight, string> = {
  normal: "400",
  bold: "600",
  bolder: "700",
} as const;

export function Text({
  color: _color = "default",
  size = 1,
  weight = "normal",
  italic = false,
  style: _style,
  ...props
}: TextProps): JSX.Element {
  const colorScheme = useColorScheme();
  const color = TEXT_COLORS[colorScheme ?? "light"][_color];
  const fontSize = BASE_TEXT_SIZE * size;
  const fontWeight = TEXT_WEIGHTS[weight];
  const fontStyle = italic ? "italic" : "normal";
  const style = [_style, { color, fontSize, fontWeight, fontStyle }];

  return <_Text style={style} {...props} />;
}
