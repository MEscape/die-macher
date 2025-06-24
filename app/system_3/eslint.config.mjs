import { dirname } from "path";
import { fileURLToPath } from "url";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
  // Add linterOptions to handle reportUnusedDisableDirectives
  recommendedConfig: { linterOptions: { reportUnusedDisableDirectives: true } }
});

const eslintConfig = [
  // Add global linterOptions
  {
    linterOptions: {
      reportUnusedDisableDirectives: true
    }
  },

  ...compat.extends(
      "next/core-web-vitals",
      "next",
      "next/typescript",
      "prettier"
  ),

  ...compat.plugins("prettier", "unused-imports", "simple-import-sort"),

  // Define rules
  {
    rules: {
      "prettier/prettier": "error",

      "no-console": "warn",
      "eqeqeq": ["error", "always"],
      "curly": "error",
      "@next/next/no-html-link-for-pages": "off",
      "no-unused-vars": ["warn", { vars: "all", args: "after-used", ignoreRestSiblings: true }],
      "unused-imports/no-unused-imports": "error",
      "unused-imports/no-unused-vars": [
        "warn",
        {
          vars: "all",
          varsIgnorePattern: "^_",
          args: "after-used",
          argsIgnorePattern: "^_"
        }
      ],
      "import/order": [
        "warn",
        {
          groups: [["builtin", "external"], "internal", "parent", "sibling", "index"],
          "newlines-between": "always"
        }
      ],
      "simple-import-sort/imports": "warn",
      "simple-import-sort/exports": "warn",
    },
  },
];

export default eslintConfig;
