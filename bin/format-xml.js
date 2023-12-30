#!/usr/bin/env node
const { relative, resolve, extname, join } = require("path");
const { readFile, writeFile } = require("fs/promises");
const formatXml = require("xml-formatter");
const readdir = require("recursive-readdir");

const PROJECT_ROOT = resolve(__dirname, "..");
const ANDROID_SRC_ROOT = join(PROJECT_ROOT, "android", "app", "src");

async function time(m, callback) {
  const started = Date.now();
  const result = await callback();
  const elapsed = Date.now() - started;

  const getMessage = typeof m === "function" ? m : () => m;
  const message = getMessage(result);

  if (message) {
    if (!/\$t/.test(message)) {
      throw new Error(`message is missing $t: \`${message}\``);
    }

    console.log(getMessage(result).replace(/\$t/g, `${elapsed}`));
  }

  return result;
}

async function main() {
  await time(
    (count) => `Formatted ${count} files in $t ms`,
    async () => {
      const files = await readdir(ANDROID_SRC_ROOT, [
        (file, stats) => stats.isFile() && extname(file) !== ".xml",
      ]);

      let count = 0;

      for (const file of files) {
        const relativePath = relative(PROJECT_ROOT, file);
        await time(
          (wrote) => (wrote ? `Formatted ${relativePath} in $t ms` : undefined),
          async () => {
            const source = await readFile(file, { encoding: "utf-8" });
            const formatted =
              formatXml(source, {
                lineSeparator: "\n",
                whiteSpaceAtEndOfSelfclosingTag: true,
              }) + "\n";

            if (source !== formatted) {
              await writeFile(file, formatted, { encoding: "utf-8" });
              count++;
              return true;
            }

            return false;
          }
        );
      }

      return count;
    }
  );
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
