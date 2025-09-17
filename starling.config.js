/**
 * Power By Starling
 */
module.exports = {
  //  收集入口
  entry: ".",

  //  剔除路径(glob)
  exclude: [
    "**/locale/**/*.*",
    "./ck_editor",
    "./ck_record",
    "./library_core",
    "./gradle",
    "./avatar"
  ],

  //  环境设置
  env: process.env.NODE_ENV === "production" ? "prod" : "dev",

  //  预览设置
  preview: {
    profile: true,
    browser: true,
    output: {
      path: "./starling",
      type: ["json", "xlsx", "html"]
    },
  },

  //  Starling项目
  starling:  {
    projectId: "555be460906e11eba731ab465b49129e",
    namespace: [
      "starlingDemo"
    ],
    mode: "normal",
    source: true,
    download: {
      path: "./locales"
    },
    upload: {
      path: "./starling/starling.xlsx",
      target: true
    }
  },

  //   文件加载器  File loader
  loaders: [
    {
      name: "starling-default-loader",
      options: {
        rules: [
          /formatMessage\(.*\)/i,
          /\s*i18n\.t\(.+\)\s*/i
        ],
        comment: true
      }
    },
    {
      name: "starling-css-loader",
      options: {
        comment: true
      }
    }
  ],

  //  生命周期plugin
  plugins: [
    {
      name: "starling-key-generator-plugin",
      options: {
        remote: {
          source: true
        },
        auto: {
          type: "machineTranslate"
        }
      }
    },
    {
      name: "starling-code-generator-plugin",
      options: {
        statement: "I18n.t(\"$key\", $variable, \"$defaultMessage\")",
        comment: {
            engine: 'google', // 机器翻译引擎 默认值: google,
            bilingual: true, // 是否是需要双语保留 默认值false
        }
      }
    }
  ],

  //  扩展命令

};
