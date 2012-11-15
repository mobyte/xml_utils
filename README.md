xml_utils
=========

Suppose we have xml document

```xml
<?xml version="1.0"?>
<cv description="test document for xml magic">
  <skills>
    <games>
      <game name="quake" version="3">
        <experience>3</experience>
      </game>
      <game name="portal" version="1">
        <experience>3</experience>
      </game>
      <game name="portal" version="2">
        <experience>2</experience>
      </game>
      <game name="half life" version="1">
        <experience>5</experience>
      </game>
      <game name="half life" version="2">
        <experience>3</experience>
      </game>
      <game name="doom" version="2">
        <experience>10</experience>
      </game>
    </games>
    <sports>
      <sport name="rma">
        <experience>5</experience>
      </sport>
      <sport name="basketball">
        <experience>10</experience>
      </sport>
    </sports>
  </skills>
  <personal fname="John" lname="Doh">
  </personal>
</cv>
```
Prepare parsed xml (using clojure.xml/parse)
```clj
(def xml (parse xml-string))
```

###Option 1

Let's retrieve first and last names from "personal" section

```clj
(def r-p-fname (xattr :first-name [:personal] :fname))
(def r-p-lname (xattr :last-name [:personal] :lname))

(r-p-fname xml)
=> {:first-name "John"}

(r-p-lname xml)
=> {:last-name "Doh"}
```
or get values by composite request
```clj
(def r-p-name (xcomp r-p-fname r-p-lname))

(r-p-name xml)
=> {:last-name "Doh", :first-name "John"}
```

Now let's get "sport experience"
```clj
(def r-s-exp (xval :sport-experience [:experience]))
(def r-s-name (xattr :sport-name [] :name))
(def r-s (xseq :sports
               [:skills :sports :sport]
               (xcomp r-s-name r-s-exp)))

(r-s xml)
=> {:sports
    ({:sport-experience "5", :sport-name "rma"}
     {:sport-experience "10", :sport-name "basketball"})}
```

And finally we can get all data including "games experience"
```clj
(def r-g-exp (xval :game-experience [:experience]))
(def r-g-name (xattr :game-name [] :name))
(def r-g-version (xattr :game-version [] :version))
(def r-g (xseq :games
               [:skills :games :game]
               (xcomp r-g-name r-g-version r-g-exp)))

(def r (xcomp r-g r-s (xcomp r-p-fname r-p-lname)))

(r xml)
=> {:first-name "John",
    :last-name "Doh",
    :sports
    ({:sport-experience "5", :sport-name "rma"}
     {:sport-experience "10", :sport-name "basketball"}),
    :games
    ({:game-experience "3", :game-version "3", :game-name "quake"}
     {:game-experience "3", :game-version "1", :game-name "portal"}
     {:game-experience "2", :game-version "2", :game-name "portal"}
     {:game-experience "5", :game-version "1", :game-name "half life"}
     {:game-experience "3", :game-version "2", :game-name "half life"}
     {:game-experience "10", :game-version "2", :game-name "doom"})}
```

###Option 2

Another way is to prepare mapping
```clj
(def schema {:games [:seq [[:skills :games :game]
                           {:game-experience [:val  [:experience]]
                            :game-name       [:attr [[] :name]]
                            :game-version    [:attr [[] :version]]}]]
             
             :sports [:seq [[:skills :sports :sport]
                            {:sport-experience [:val [:experience]]
                             :sport-name       [:attr [[] :name]]}]]
             
             :first-name [:attr [[:personal] :fname]]
             :last-name  [:attr [[:personal] :lname]]})
```

and create request that getting all described in mapping data at once

```clj
(def r (xschema schema))

(r r/xml)
=> {:last-name "Doh",
    :first-name "John",
    :sports
    ({:sport-name "rma", :sport-experience "5"}
     {:sport-name "basketball", :sport-experience "10"}),
    :games
    ({:game-version "3", :game-name "quake", :game-experience "3"}
     {:game-version "1", :game-name "portal", :game-experience "3"}
     {:game-version "2", :game-name "portal", :game-experience "2"}
     {:game-version "1", :game-name "half life", :game-experience "5"}
     {:game-version "2", :game-name "half life", :game-experience "3"}
     {:game-version "2", :game-name "doom", :game-experience "10"})}
```