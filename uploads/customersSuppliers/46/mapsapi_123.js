$ = jQuery;
$(document).ready(function () {

    $.fn.select2.amd.define('select2/data/googleAutocompleteAdapter', ['select2/data/array', 'select2/utils'],
        function (ArrayAdapter, Utils) {
            var googleAutocompleteOptions = {
                types: ['geocode'],
                componentRestrictions: {country: "fr"}
            };
            function GoogleAutocompleteDataAdapter($element, options) {
                GoogleAutocompleteDataAdapter.__super__.constructor.call(this, $element, options);
            }

            Utils.Extend(GoogleAutocompleteDataAdapter, ArrayAdapter);

            GoogleAutocompleteDataAdapter.prototype.query = function (params, callback) {
                var returnSuggestions = function (predictions, status) {
                    var data = {results: []};
                    if (status != google.maps.places.PlacesServiceStatus.OK) {
                        callback(data);
                    }
                    for (var i = 0; i < predictions.length; i++) {
                        // console.log(predictions[i]);
                        data.results.push({id: predictions[i].description, text: predictions[i].description});
                    }
                    data.results.push({id: ' ', text: 'Powered by Google', disabled: true});
                    callback(data);
                };

                if (params.term && params.term != '') {
                    var service = new google.maps.places.AutocompleteService();
                    service.getPlacePredictions({input: params.term , types: ['geocode'], componentRestrictions: {country: "fr"}}, returnSuggestions);
                } else {
                    var data = {results: []};
                    data.results.push({id: ' ', text: 'Powered by Google', disabled: true});
                    callback(data);
                }
            };
            return GoogleAutocompleteDataAdapter;
        }
    );

    function formatRepo(repo) {
        if (repo.loading) {
            return repo.text;
        }

        var markup = "<div class='select2-result-repository clearfix'>" +
            "<div class='select2-result-title'>" + repo.text + "</div>";
        return markup;
    }

    function formatRepoSelection(repo) {
        return repo.text;
    }

    var googleAutocompleteAdapter = $.fn.select2.amd.require('select2/data/googleAutocompleteAdapter');
    setTimeout(function () {
        $('#canton').select2('destroy');

    $('#canton').select2({
        width: '100%',
        dataAdapter: googleAutocompleteAdapter,
        placeholder: ' Saisissez votre ville ou code postal',
        escapeMarkup: function (markup) {
            return markup;
        },
        minimumInputLength: 2,
        templateResult: formatRepo,
        templateSelection: formatRepoSelection
    });
    }, 300);
    function initialize() {
        var input = document.getElementById('address-447');
        var input2 = document.getElementById('canton_55');
        const autocomplete = new google.maps.places.Autocomplete(input);
        const autocomplete2 = new google.maps.places.Autocomplete(input2);
        // Set initial restrict to the greater list of countries.
        autocomplete.setComponentRestrictions({
            country: ["fr"],
        });
        autocomplete2.setComponentRestrictions({
            country: ["fr"],
        });
        // new google.maps.places.Autocomplete(input);
    }

    google.maps.event.addDomListener(window, 'load', initialize);
});