import path from 'path';
import webpack from 'webpack';
import {CleanWebpackPlugin} from 'clean-webpack-plugin';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import CircularDependencyPlugin from 'circular-dependency-plugin';
import ForkTsCheckerWebpackPlugin from 'fork-ts-checker-webpack-plugin';
import FriendlyErrorsWebpackPlugin from 'friendly-errors-webpack-plugin';
import postcssReporter from 'postcss-reporter';
import postcssSCSS from 'postcss-scss';
import autoprefixer from 'autoprefixer';

import threadLoaderLib from 'thread-loader';
import {getEnvParams} from "../params";


export type BuildType = 'dev' | 'prod';

const {chunkHash, chunkName, withHot, isWatchMode} = getEnvParams();

const threadLoader: webpack.Loader[] = (() => {
    if (process.env.THREADED === 'true') {
        const workerPool = {
            workers: require('os').cpus().length - 1,
            poolTimeout: withHot ? Infinity : 2000,
        };
        isWatchMode && threadLoaderLib.warmup(workerPool, [
            'babel-loader',
            'ts-loader',
            'postcss-loader',
            'sass-loader',
        ]);
        return [{loader: 'thread-loader', options: workerPool}];
    }
    return [];
})();

function sortChunks(a: webpack.compilation.Chunk, b: webpack.compilation.Chunk) {
    const order = ['app', 'vendors', 'runtime'];
    return order.findIndex(
        // webpack typings for Chunk are not correct wait for type updates for webpack.compilation.Chunk
        item => (b as any).names[0].includes(item),
    ) - order.findIndex(item => (a as any).names[0].includes(item));
}

export const getCommonPlugins: (type: BuildType) => webpack.Plugin[] = type => [
    new CleanWebpackPlugin({
        cleanOnceBeforeBuildPatterns: ['build', 'static']
    }),
    new MiniCssExtractPlugin({
        filename: `css/[name].[${chunkHash}].css`,
        chunkFilename: `css/[id].[${chunkHash}].css`,
    }),
    new HtmlWebpackPlugin({
        filename: 'index.html',
        template: 'assets/index.html',
        chunksSortMode: sortChunks,
    }),
    new CircularDependencyPlugin({
        exclude: /node_modules/,
        failOnError: true,
        cwd: process.cwd(),
    })
]
    .concat(type !== 'prod' ? (
        new ForkTsCheckerWebpackPlugin({
            checkSyntacticErrors: true,
            async: false,
            tsconfig: path.resolve('./tsconfig.json'),
        })) : [])
    .concat(type !== 'prod' ? (
        new FriendlyErrorsWebpackPlugin()
    ) : [])
    .concat(withHot && type !== 'prod' ? (
        new webpack.HotModuleReplacementPlugin()
    ) : []);


export const getCommonRules: (type: BuildType) => webpack.RuleSetRule[] = type => [
    {
        test: /\.tsx?$/,
        use:
            threadLoader
                .concat(withHot && type === 'dev' ? {
                    loader: 'babel-loader',
                    options: {
                        babelrc: false,
                        cacheDirectory: true,
                        plugins: [
                            'react-hot-loader/babel',
                            'syntax-dynamic-import',
                        ],
                    },
                } : [])
                .concat({
                    loader: 'ts-loader',
                    options: {
                        transpileOnly: true,
                        happyPackMode: true,
                        logLevel: 'error',
                    },
                }),
    },
    {
        test: /\.(ttf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
        use: 'file-loader?name=fonts/[hash].[ext]',
    },
    {
        test: /\.(png|svg)/,
        loader: 'url-loader',
        options: {
            name: 'images/[name].[ext]',
            limit: 10000,
        },
    }
];

const commonScssLoaders: webpack.Loader[] = [
    {
        loader: 'postcss-loader',
        options: {
            plugins: () => [
                autoprefixer({}),
            ],
        },
    },
    'sass-loader',
    {
        loader: 'postcss-loader',
        options: {
            syntax: postcssSCSS,
            plugins: () => [
                postcssReporter({
                    clearReportedMessages: true,
                    throwError: true,
                }),
            ],
        },
    },
    "@teamsupercell/typings-for-css-modules-loader"
];

export function getStyleRules(type: BuildType) {
    const getCssLoader = (extend: boolean = false, onlyLocals: boolean = false) => ({
        loader: 'css-loader',
        options: {
            onlyLocals, importLoaders: 1,
            modules: {
                localIdentName: extend ? '[name]__[local]' : '[hash:base64:5]'
            },
            sourceMap: extend,
        },
    });

    const cssLoaders: Record<BuildType, webpack.Loader[]> = {
        dev: ['style-loader', 'css-loader'],
        prod: [MiniCssExtractPlugin.loader, 'css-loader'],
    };

    const scssFirstLoaders: Record<BuildType, webpack.Loader[]> = {
        dev: ['style-loader', getCssLoader(true)],
        prod: [MiniCssExtractPlugin.loader, getCssLoader(false)],
    };

    return [
        {
            test: /\.css$/,
            use: cssLoaders[type],
        },
        {
            test: /\.scss$/,
            use: scssFirstLoaders[type].concat(commonScssLoaders),
        },
    ];
}

export const commonConfig: webpack.Configuration = {
    target: 'web',
    context: path.resolve(__dirname, '..', '..', 'src'),
    output: {
        publicPath: '/',
        path: path.resolve(__dirname, '..', '..', 'build'),
        filename: `js/[name]-[${chunkHash}].bundle.js`,
        chunkFilename: `js/[${chunkName}]-[${chunkHash}].bundle.js`,
    },
    resolve: {
        modules: ['node_modules', 'src'],
        extensions: ['.js', '.jsx', '.ts', '.tsx'],
    },
    optimization: {
        runtimeChunk: 'single',
        splitChunks: {
            chunks: 'all',
        },
    },
    stats: {
        // typescript would remove the interfaces but also remove the imports of typings
        // and because of this, warnings are shown https://github.com/TypeStrong/ts-loader/issues/653
        warningsFilter: /export .* was not found in/,
    }
};
