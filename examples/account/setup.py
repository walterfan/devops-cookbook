from setuptools import find_packages, setup

setup(name="microservices",
      version = "0.1",
      description = "account management",
      author = "Walter",
      platforms = ["any"],
      license = "BSD",
      packages = find_packages(),
      install_requires = ["Flask==0.10.1", "requests==2.20.0", "wsgiref==0.1.2" ],
      )