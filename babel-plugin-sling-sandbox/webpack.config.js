module.exports = {    
  entry: './lib/index.js',
  output: {
    filename: './dist/sling-babel.js',
    library: "SlingBabel",
    libraryTarget: "this"      
  },
 module: {
  loaders: [
    {
      test: /\.js$/,
      exclude: /(node_modules|bower_components)/,
      loader: 'babel-loader',
      query: {
        presets: ['es2015']
      }
    }
  ]
}
}