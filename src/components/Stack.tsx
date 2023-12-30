import React, { Children, Fragment, ReactNode } from "react";
import { StyleProp, View, ViewStyle } from "react-native";

export type StackProps = {
  children: ReactNode;
  style?: StyleProp<ViewStyle> | undefined;
  childStyle?: StyleProp<ViewStyle> | undefined;
} & (
  | { spacing: number; spacer?: undefined }
  | { spacer: JSX.Element; spacing?: undefined }
) &
  (
    | { horizontal: true; vertical?: false | undefined }
    | { vertical: true; horizontal?: false | undefined }
  );

export function Stack({
  spacing,
  spacer: _spacer,
  horizontal,
  children,
  style,
  childStyle,
}: StackProps): JSX.Element {
  const flexDirection = horizontal ? "row" : "column";
  const spacer = _spacer ?? (
    <View style={horizontal ? { width: spacing } : { height: spacing }} />
  );

  return (
    <View style={[style, { flexDirection }]}>
      {Children.toArray(children).map((child, i, array) => (
        <Fragment key={i}>
          {childStyle ? <View style={childStyle}>{child}</View> : child}
          {i < array.length - 1 ? spacer : null}
        </Fragment>
      ))}
    </View>
  );
}
