import { exec } from 'child_process';

import {
    CopyPlugin,
    CSSModulesPlugin,
    CSSPlugin,
    EnvPlugin,
    FuseBox, ImageBase64Plugin, QuantumPlugin,
    SassPlugin,
    Sparky,
    SVGPlugin,
    WebIndexPlugin
} from 'fuse-box';
import { TypeChecker } from 'fuse-box-typechecker';
import * as path from 'path';


import { getClientEnvironment } from './fuse.env';

// Configure the environment
const env = getClientEnvironment();
const isProduction = process.env.NODE_ENV === 'production';

// Configure directories
const root = path.resolve(__dirname, './');
const directory = {
    node_modules: path.join(root, 'node_modules'),
    build: path.join(root, 'build'),
    cache: path.join(root, '.fusebox'),
    public: path.join(root, 'public'),
    src: path.join(root, 'src'),
    raw: path.join(root, 'src', 'utils', 'raw'),
    assets: path.join(root, 'src', 'assets'),
    theme: path.join(root, 'src', 'theme')
};

// Configure type checker
const tsConfigPath = path.join(root, 'tsconfig.json');
const typechecker = TypeChecker({

    name: 'app',
    basePath: root,
    tsConfig: tsConfigPath,
    homeDir: '/',
    isPlugin: false,
    printFirstRun: false,
    tsConfigJsonContent: false
});
const runSyncTypeChecker = () => {
    console.log(`\x1b[36m%s\x1b[0m`, `prod app bundled - running type check`);
    typechecker.inspectAndPrint();
};

if (!isProduction) {
    // Create thread
    typechecker.inspectAndPrint();
}
const runThreadTypeChecker = () => {
    console.log(`\x1b[36m%s\x1b[0m`, `dev app bundled - running type check`);
    // Use thread, tell it to typecheck and print result
    typechecker.inspectAndPrint();
};


const stylePlugins = [
    SassPlugin(),
    CSSModulesPlugin({
       // scopedName: "[name]__[local]",
    }),
    CSSPlugin({
        group: "bundle.css",
        outFile: "build/bundle.css",
        inject: false
    })
];

const styleDevPlugins = [
    SassPlugin(),
    CSSModulesPlugin({
      //  scopedName: "[name]__[local]",
    }),
    CSSPlugin({
        group: "bundle.css",
        outFile: "build/bundle.css",
        inject: false
    })
];

// Generic fuse configuration
function getConfig() {
    const plugins: any[] = [
        EnvPlugin(env.raw),
        WebIndexPlugin({
            target: `index.html`,
            template: `${directory.public}/index.html`,
            path: '/'
        }),
        ImageBase64Plugin({
            useDefault: true
        }),
        SVGPlugin(),
        CopyPlugin({
            files: ['*.png', '*.jpg', '*.svg', '*.html, *.ico, *.json'],
            dest: 'public'
        })
    ];

    if (isProduction) {
        plugins.push(
            QuantumPlugin({
                bakeApiIntoBundle: 'app',
                css: false,
                extendServerImport: true,
                polyfills: ['Promise'],
                replaceTypeOf: false,
                target: 'browser',
                treeshake: true,
                uglify: true
            })
        );
    }

    return FuseBox.init({
        homeDir: directory.src,
        target: 'browser',
        output: `${directory.build}/$name.js`,
        sourceMaps: !isProduction,
        cache: !isProduction,
        debug: !isProduction,
        hash: false,
        log: {
            enabled: !isProduction,
            showBundledFiles: false, // Don't list all the bundled files every time we bundle
            clearTerminalOnBundle: false // Clear the terminal window every time we bundle
        },
        allowSyntheticDefaultImports: true,
        useTypescriptCompiler: true,
        plugins
    });
}

// Clean the build directory
Sparky.task('clean', [], async () => {
    await Sparky.src(`${directory.cache}`)
        .clean(`${directory.cache}`)
        .exec();

    return await Sparky.src(`${directory.build}`)
        .clean(`${directory.build}`)
        .exec();
});

// Copy static content / assets
Sparky.task('copy', [], async () => {
    return new Promise((resolve, reject) => {
        exec(`cp -r ${directory.assets} ${directory.build}`, (err, stdout, stderr) => {
            resolve();
        });

        exec(`cp -r ${directory.public} ${directory.build}`, (err, stdout, stderr) => {
            resolve();
        });
    });
});

Sparky.task('worker', [], async () => {
    const fuseInstance = FuseBox.init({
        homeDir: directory.src + '/worker/',
        target: 'browser',
        output: `${directory.build}/$name.js`,
        sourceMaps: !isProduction,
        cache: !isProduction,
        debug: !isProduction,
        hash: false,
        log: {
            enabled: !isProduction,
            showBundledFiles: false, // Don't list all the bundled files every time we bundle
            clearTerminalOnBundle: false // Clear the terminal window every time we bundle
        },
        allowSyntheticDefaultImports: true,
        useTypescriptCompiler: true,
        plugins: [EnvPlugin(env.raw),
            QuantumPlugin({
                containedAPI: true,
                bakeApiIntoBundle: 'worker',
                css: false,
                extendServerImport: true,
                polyfills: ['Promise'],
                replaceTypeOf: false,
                target: 'browser',
                treeshake: true,
                uglify: true
            })]
    });
    fuseInstance
        .bundle('worker')
        .tsConfig(tsConfigPath)
        .target('browser')
        .instructions(`> index.tsx`);
    return await fuseInstance.run();
});

Sparky.task('production', [], async () => {
    const fuseInstance = getConfig();
    fuseInstance
        .bundle('app')
        .tsConfig(tsConfigPath)
        .target('browser')
        .plugin([...stylePlugins])
        .completed(() => {
            runSyncTypeChecker();
        })
        .splitConfig({dest: '/chunks/'})
        .instructions(`> index.tsx`);
    return await fuseInstance.run();
});

Sparky.task('development', [], async () => {
    const fuseInstance = getConfig();
    fuseInstance.dev({fallback: 'index.html'});
    fuseInstance
        .bundle('app')
        .tsConfig(tsConfigPath)
        .target('browser')
        .plugin([...styleDevPlugins])
        .completed(() => {
            runThreadTypeChecker();
        })
        .watch()
        .instructions(`> index.tsx`)
        .hmr();
    return await fuseInstance.run();
});

Sparky.task('build', ['clean', 'copy', 'production', 'worker'], () => {
    return;
});
Sparky.task('default', ['clean', 'copy', 'development'], () => {
    return;
});
