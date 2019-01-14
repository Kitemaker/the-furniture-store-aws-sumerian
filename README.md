# The-Furniture-Store
Next Level Digital Signage supported by 3D models and Augmented Reality Application

## Inspiration
As per wikipedia  [link](https://en.wikipedia.org/wiki/Digital_signage) over 200 different companies worldwide market digital signage solutions and the number is still consistently growing. With the influx of digital signage providers, the digital signage market is expected to grow upward to $21.92 Billion USD by 2020, with a CAGR of 8.04% from 2015 to 2020.
  Using AWS Sumerian a digital signage with dynamic content which can be updated over the web , providing latest updates for the products, instant updates on the offers can be provided. Considering the industry segment for the Home and office furniture , A digital signage is required which can provide 3D models, customised offers and instant purchasing options by just scanning the digital signage. In this project I have tried to provide a digital signage with latest features which can attract users in the shop or showrooms and will be attractive in look n feel also. thanks to the multiple features provided by Amazon Sumerian
 
## What it does

The Furniture Store is Sumerian and AR based Digital Signage for a dummy company called ' The Furniture Store'
Following are the main features:
### Highly Dynamic Content 
can be updated using the AWS Sumerian. and interface is provided with AWS S3 to provide latest product images and designs
### 3D Models of the Product 
3D models from Sumerian Assets are used to integrate in the screen. State machines and scripts are used to display and rotate the models
### AR Application For Real Time Experience
ARCore application has been provided, using which user can scan the QR Code of the products provided on the screen. Seller can integrate the offer code also in the QR Code which can be customised as per the user login.
Once the QR Code is scanned in the Mobile Application , models shall be available in the App  so that user can place it in the real world to have a real time experience and find the suitability of color, size and design of the products.
Further.
### Product Advertising API link
Product advertising API frame has been linked with the QR Code. So when a code is matched with the product , related html iframe shall be available inside the mobile app , this frame is associated with Amazon Product Advertising API, by using the link a user can decide to purchase the product and it will benefit to the Associate also. Customised offer can be provided using this link with respect to the user accounts if user step-in at a perticular store and scan the QR Code.
### Open Weather Map Widget
Just to attract the customers a widget from https://openweathermap.org/ is integrated in a html 3D element, to provide city's weather update and forecast. Some other widget e.g. stock market upgrade, Currency rates or news headlines can also be used in this corner to make the digital sign more attractive.  


## How I built it
This Next-Level AR enabled Digital Signage is based on AWS Sumerian. To link with Amazon website and Amazon associate program , models from Amazon assets ( available with ASIN ) have been used.

## Challenges I ran into

## Accomplishments that I'm proud of

## What I learned

## What's next for The Furniture Store 

