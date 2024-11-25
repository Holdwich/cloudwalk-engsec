# Intro

This is my answer to the Cloudwalk EngSec test, this will describe my thinking process, as well as accompany any evidences that i found to be of interest, and my personal thoughts on how to analyse the dataset given to me (and inserted into this repo)

## First steps

First of all, what i did first, given the CSV dataset, was install Grafana, and use it to interpret the CSV with the infinity plugin.

![Evidence 1](/photos/1.png?raw=true "Evidence 1")

With the data now loaded into Grafana, first thing i did was some data transformation, achieving a count of all the client IPs present in the dataset, and sorting them by their IP, and here is where i found my first probable risk

### 1. Unusual IP activities and URLs

![Evidence 2](/photos/2.png?raw=true "Evidence 2")

Many IPs only accessed the server once, but some outliers accessed the server upwards of 100 times, with the IP **53.153.77.110** accessing the server **156 times**

While this, by itself, might not be certainly a indicator of a security incident, it points towards anomalous activities

![Evidence 3](/photos/3.png?raw=true "Evidence 3")

The accesses seem not to follow a pattern on "when" they access the server, but they do have something in common, they all access they same endpoint (/write/toward/story) except for a few requests that are suspicious

Request 1 = /../../../windows/win.ini, trying to access the filesystem of the server
Request 2, 3 and 4 = ```/<marquee><img src=1 onerror=alert(1)></marquee>, /";!--"<XSS>=&{()}, /%00%01%02%03%04%05%06%07```, trying to insert scripts/unsanitized inputs into the server

These requests types repeat more than once, all of this, points to a bad actor

![Evidence 4](/photos/4.png?raw=true "Evidence 4")

Then, i went on to check these paths, how many times they appear, by counting these paths, and sorting them by each type of request

![Evidence 5](/photos/5.png?raw=true "Evidence 5")

We can see that there are many attempts of the kind, and coming from different IPs

![Evidence 6](/photos/6.png?raw=true "Evidence 6")

Another thing I also went ahead and checked after, was the byte size of the request, sure enough, giving me more evidence of attempts to access the server and exfiltrate data

![Evidence 9](/photos/9.png?raw=true "Evidence 9")

### My Solution

To address this problem hardening the server and application security is a must, this includes validation of input, such as in Java (see example_secure_api_implementation.java):

```
File file = new File(BASE_DIRECTORY, userInput);
if (file.getCanonicalPath().startsWith(BASE_DIRECTORY)) {
    // process file
}
```

and to use SAST tools to ensure that the server application follows security patterns

a WAF (Web Application Firewall) can also be used to block/throttle anomalous traffic, and a IDS/IPS can also be crucial to identify and prevent any anomalous accesses to the server

### 2. Geographical Monitoring

Next, i went on to check the ClientCountry flag, counting each time that a country appears, trying to identify if there is a pattern between the attackers and the countries

While nothing of substance was found by me, i did find something:

![Evidence 7](/photos/7.png?raw=true "Evidence 7")

Most of these regions of access are common, but cn, China, is considered a risk country, and thus, blocking it should be considered

India can be considered a risk country too, but more context is needed to make a choice

### My Solution

To block China (and any other risk country) from accessing the server, a WAF (Web Application Firewall) can be used, such as Cloudflare's:

```
ip.geoip.country eq "CN"
```

### 3. Ports

Next, i went on to check the ports, a common vector of attack

By doing the same analysis of counting and summarizing the ports used, i could find that various accesses used different ports, but one of them stood out to me, Port **0**

![Evidence 8](/photos/8.png?raw=true "Evidence 8")

While a deeper analysis and context is needed to determine which ports are indeed needed for the appropriate functioning of the server, 0 is not one of them

### My Solution

Audit and restrict port usage

Firewalls should be used to block any port that is not essential for the functioning of the server, and tools like nmap should be used to scan for open and vulnerable ports

# Conclusion

I did my due dilligence with the tools i have, and the expertise i have; while i might have missed something, i believe i did get most of the events that could be logged as incidents

My final considerations are as follows:

- Log analysis tools such as Grafana are needed to the extreme in cases such as this, with 0 clues, i found some vulnerabilities that would otherwise take ages during manual check
- Automation tools such as SOAR systems, WAF providers and SATS can be used to help with the prevention of cyberattacks
- Cybersecurity should always be part of the development cycle (again, SATS can be used)




