const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
module.exports = {
    entry: { 
                main: './app/js/main.js'
            },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, '../gateway/src/main/resources/static/dist')
    },
    resolve: {
        alias: {
            $: "jquery/src/jquery",
        }
    },
    mode: 'development',
    //devtool: 'source-map',
module: {
        rules: [{
            test: /\.js$/,
            include: [path.resolve(__dirname, "./src/app")],
            exclude: /node_modules/,
            use: {
                loader: 'babel-loader',
                options: {
                    presets: ['env']
                }
            }
        },
        {
            test: /\.(css)$/,
            use: [ MiniCssExtractPlugin.loader, 'css-loader' ],
        },
        {
            test: /\.(png|svg|jpg|jpeg|gif)$/i,
            type: 'asset/resource',
          }]
    },
    plugins: [new MiniCssExtractPlugin()]
};
