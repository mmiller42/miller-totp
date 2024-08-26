import { NativeModules } from "react-native";
import { AppData } from "../types";

export type BindWidgetsModule = {
  getWidgetIds: () => Promise<number[]>;
  syncSettings(data: AppData): Promise<null>;
};

const Module: BindWidgetsModule = NativeModules.BindWidgetsModule;
const { getWidgetIds, syncSettings } = Module;

export { getWidgetIds, syncSettings };
export default Module;
