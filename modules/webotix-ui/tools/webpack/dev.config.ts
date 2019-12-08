import webpack from 'webpack';

import {getCommonRules, commonConfig, getStyleRules, BuildType, getCommonPlugins} from './common';

import path from "path";
import {getEnvParams} from "../params";

const {withHot} = getEnvParams();

const getDevConfig: (type?: BuildType) => webpack.Configuration = type => {
    const rules: webpack.RuleSetRule[] = [
        ...getCommonRules(type || 'dev'),
        ...getStyleRules(type || 'dev'),
    ];

    return {
        ...commonConfig,
        devServer: {
            hot: withHot,
            contentBase: path.resolve('..', 'build'),
            host: '0.0.0.0',
            port: 8080,
            inline: true,
            lazy: false,
            historyApiFallback: true,
            disableHostCheck: true,
            stats: {
                colors: true,
                errors: true,
                errorDetails: true,
                warnings: true,
                assets: false,
                modules: false,
                warningsFilter: /export .* was not found in/,
            },
        },
        mode: 'development',
        devtool: 'source-map',
        entry: {
            app: './index.tsx',
        },
        module: {
            rules,
        },
        plugins: getCommonPlugins(type || 'dev'),
    };
};

export default getDevConfig;
