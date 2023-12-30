import React, { ForwardedRef, forwardRef } from "react";
import {
  ColorSchemeName,
  StyleSheet,
  TextInput as _TextInput,
  TextInputProps,
  useColorScheme,
} from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";

type NonNilColorScheme = Extract<ColorSchemeName, string>;

const BORDER_COLORS: Record<NonNilColorScheme, string> = {
  light: Colors.dark,
  dark: Colors.light,
} as const;

export const TextInput = forwardRef(
  ({ style, ...props }: TextInputProps, ref: ForwardedRef<_TextInput>) => {
    const colorScheme = useColorScheme();
    const borderColor = BORDER_COLORS[colorScheme ?? "light"];

    return (
      <_TextInput
        ref={ref}
        style={[styles.textInput, { borderColor }, style]}
        {...props}
      />
    );
  }
);

const styles = StyleSheet.create({
  textInput: {
    marginVertical: 8,
    paddingHorizontal: 8,
    borderWidth: 1,
  },
});
