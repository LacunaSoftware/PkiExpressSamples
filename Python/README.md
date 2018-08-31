PKI Express Python Sample
========================

This folder contains a web application written in Python using the Flask framework, that shows how to use the
[PKI Express](http://docs.lacunasoftware.com/en-us/articles/pki-express/). The sample application
should work on Python.

> To use PKI Express, you'll need a license file. Please [contact us](https://www.lacunasoftware.com/en/home/purchase)
> to get a free trial license.

Running the sample
------------------

To run the sample:

1. [Install PKI Express](https://docs.lacunasoftware.com/articles/pki-express/setup/)
1. [Download the project](https://github.com/LacunaSoftware/PkiExpressSamples/archive/master.zip)
   or clone the repository
1. In a command prompt, navigate to the folder `Python` and run the command `pip install -r requirements.txt` to
   download the dependencies.
1. Set the `FLASK_APP` environment variable to define the name of app that
   should be run: `FLASK_APP=sample`
1. Run the web application: `flask run`
1. Access the URL [http://localhost:5000](http://localhost:5000)

Optionally, you can create and activate a "virtualenv" to avoid mixing library versions:

    virtualenv <venv>
    source bin/activate (on Windows: ./<venv>/Scripts/activate)

## Documentation

https://docs.lacunasoftware.com/articles/pki-express/

